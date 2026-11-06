package com.dongeria.split

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongeria.split.ui.theme.SplitTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil

/** Quick-pick tip percentages shown as chips. */
private val TIP_PRESETS = listOf(10, 12, 15, 18)
private const val DEFAULT_TIP_PERCENT = 12.0
private const val MAX_PEOPLE = 50

/** SharedPreferences used to remember the last tip percentage between app launches. */
private const val PREFS_NAME = "split_prefs"
private const val KEY_LAST_TIP = "last_tip_percent"

private fun loadLastTip(context: Context): String =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LAST_TIP, "") ?: ""

private fun saveLastTip(context: Context, tip: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_LAST_TIP, tip)
        .apply()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SplitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SplitLayout()
                }
            }
        }
    }
}

/** Result of a tip calculation. Pure data, so it is easy to unit test. */
data class TipResult(val tip: Double, val total: Double, val perPerson: Double)

/**
 * Calculates tip, total and per-person share.
 * When [roundUp] is true the tip is always rounded UP to the next whole shekel.
 */
fun calculateTip(
    amount: Double,
    tipPercent: Double,
    roundUp: Boolean,
    people: Int = 1
): TipResult {
    var tip = tipPercent / 100 * amount
    if (roundUp) tip = ceil(tip)
    val total = amount + tip
    return TipResult(tip = tip, total = total, perPerson = total / people.coerceAtLeast(1))
}

/**
 * Keeps only digits and a single decimal point (max 2 decimals).
 * A comma is treated as a decimal point, since some keyboards use it.
 */
internal fun sanitizeNumber(input: String): String {
    val filtered = input.replace(',', '.').filter { it.isDigit() || it == '.' }
    val dot = filtered.indexOf('.')
    if (dot == -1) return filtered
    val intPart = filtered.substring(0, dot)
    val decimals = filtered.substring(dot + 1).replace(".", "").take(2)
    return "$intPart.$decimals"
}

@Composable
fun SplitLayout() {
    // rememberSaveable keeps the values when the screen rotates.
    var amountInput by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    // The last tip percentage is loaded from SharedPreferences on first launch.
    var tipInput by rememberSaveable { mutableStateOf(loadLastTip(context)) }
    val updateTip: (String) -> Unit = { newTip ->
        tipInput = newTip
        saveLastTip(context, newTip)
    }
    var roundUp by rememberSaveable { mutableStateOf(false) }
    var people by rememberSaveable { mutableIntStateOf(1) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: DEFAULT_TIP_PERCENT
    val result = calculateTip(amount, tipPercent, roundUp, people)

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("he-IL")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        EditNumberField(
            label = R.string.bill_amount,
            leadingIcon = R.drawable.money,
            value = amountInput,
            onValueChange = { amountInput = sanitizeNumber(it) },
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth()
        )

        EditNumberField(
            label = R.string.how_was_the_service,
            leadingIcon = R.drawable.percent,
            value = tipInput,
            placeholder = "${DEFAULT_TIP_PERCENT.toInt()}",
            onValueChange = { updateTip(sanitizeNumber(it)) },
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth()
        )

        TipPresetChips(
            selected = tipInput.toDoubleOrNull(),
            onSelect = { updateTip(it.toString()) }
        )

        PeopleStepper(
            people = people,
            onPeopleChange = { people = it.coerceIn(1, MAX_PEOPLE) }
        )

        RoundTheTipRow(roundUp = roundUp, onRoundUpChanged = { roundUp = it })

        ResultCard(
            tip = formatter.format(result.tip),
            total = formatter.format(result.total),
            perPerson = formatter.format(result.perPerson),
            showPerPerson = people > 1
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EditNumberField(
    @StringRes label: Int,
    @DrawableRes leadingIcon: Int,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(stringResource(label)) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}

@Composable
fun TipPresetChips(selected: Double?, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TIP_PRESETS.forEach { percent ->
            FilterChip(
                selected = selected == percent.toDouble(),
                onClick = { onSelect(percent) },
                label = { Text("$percent%") }
            )
        }
    }
}

@Composable
fun PeopleStepper(people: Int, onPeopleChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.split_between),
            modifier = Modifier.weight(1f)
        )
        FilledTonalIconButton(onClick = { onPeopleChange(people - 1) }, enabled = people > 1) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = people.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )
        FilledTonalIconButton(onClick = { onPeopleChange(people + 1) }, enabled = people < MAX_PEOPLE) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.round_up_tip), modifier = Modifier.weight(1f))
        Switch(checked = roundUp, onCheckedChange = onRoundUpChanged)
    }
}

@Composable
fun ResultCard(tip: String, total: String, perPerson: String, showPerPerson: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.tip_label), style = MaterialTheme.typography.labelLarge)
            Text(tip, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ResultRow(stringResource(R.string.total_label), total)
            if (showPerPerson) ResultRow(stringResource(R.string.per_person_label), perPerson)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(name = "Hebrew (RTL)", showBackground = true, locale = "iw")
@Composable
fun SplitLayoutPreview() {
    SplitTheme {
        SplitLayout()
    }
}
