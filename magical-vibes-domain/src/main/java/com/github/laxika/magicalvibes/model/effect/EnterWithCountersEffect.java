package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * As-enters replacement effect (MTG Rule 614.1c): "This permanent enters the battlefield
 * with [count] [type] counters on it." The count is a {@link DynamicAmount} (fixed number,
 * X paid, permanent count, creature deaths this turn, …). Conditional variants ("if kicked",
 * "Raid —") wrap this in a {@link ConditionalEffect}.
 * <p>
 * Handled in {@code BattlefieldEntryService.putPermanentOntoBattlefield()} so the counters
 * are on the permanent before ETB triggers fire and before static/CDA evaluation sees it
 * (CR 614.12).
 */
public record EnterWithCountersEffect(CounterType type, DynamicAmount count, boolean countColorsSpent,
                                      CardPredicate cardPredicate)
        implements ReplacementEffect {

    public EnterWithCountersEffect(CounterType type, DynamicAmount count) {
        this(type, count, false, null);
    }

    public EnterWithCountersEffect(CounterType type, DynamicAmount count, boolean countColorsSpent) {
        this(type, count, countColorsSpent, null);
    }

    public EnterWithCountersEffect(CounterType type, DynamicAmount count, CardPredicate cardPredicate) {
        this(type, count, false, cardPredicate);
    }
}
