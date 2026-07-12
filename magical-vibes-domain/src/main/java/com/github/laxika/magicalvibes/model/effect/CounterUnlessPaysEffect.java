package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Counter target spell unless its controller pays a mana amount.
 *
 * @param amount            the generic mana amount to pay (ignored when {@code useXValue} or
 *                          {@code dynamicAmount} is set, except in the PendingMayAbility where it
 *                          carries the resolved amount)
 * @param useXValue         if true, read the amount from the stack entry's X value instead of {@code amount}
 * @param exileIfCountered  if true, exile the countered spell instead of putting it into the graveyard
 * @param dynamicAmount     when non-null, the pay amount is evaluated from this at resolution
 *                          (e.g. "{1} for each blue permanent you control", Spell Syphon)
 */
public record CounterUnlessPaysEffect(int amount, boolean useXValue, boolean exileIfCountered,
                                      DynamicAmount dynamicAmount) implements CardEffect {

    /** Fixed-amount counter-unless-pays (e.g. Mana Leak). */
    public CounterUnlessPaysEffect(int amount) {
        this(amount, false, false, null);
    }

    public CounterUnlessPaysEffect(int amount, boolean useXValue, boolean exileIfCountered) {
        this(amount, useXValue, exileIfCountered, null);
    }

    /** Dynamic-amount counter-unless-pays ("{1} for each …", Spell Syphon). */
    public CounterUnlessPaysEffect(DynamicAmount dynamicAmount) {
        this(0, false, false, dynamicAmount);
    }

    @Override
    public boolean canTargetSpell() {
        return true;
    }
}
