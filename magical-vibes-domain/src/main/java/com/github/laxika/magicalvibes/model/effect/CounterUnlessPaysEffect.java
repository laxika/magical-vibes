package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import java.util.List;

/**
 * Counter target spell unless its controller pays a mana amount (and optionally life).
 *
 * @param amount            the generic mana amount to pay (ignored when {@code useXValue} or
 *                          {@code dynamicAmount} is set, except in the PendingMayAbility where it
 *                          carries the resolved amount)
 * @param useXValue         if true, read the amount from the stack entry's X value instead of {@code amount}
 * @param exileIfCountered  if true, exile the countered spell instead of putting it into the graveyard
 * @param dynamicAmount     when non-null, the pay amount is evaluated from this at resolution
 *                          (e.g. "{1} for each blue permanent you control", Spell Syphon)
 * @param onNotPaidEffects  effects resolved against the countered spell's controller when they do not
 *                          pay (their target id is set to that player), e.g. Power Sink's "they tap all
 *                          lands with mana abilities they control and lose all unspent mana"
 * @param lifeCost          additional life that must be paid together with the mana (Mundungu:
 *                          "{1} and 1 life"); {@code 0} means mana only
 */
public record CounterUnlessPaysEffect(int amount, boolean useXValue, boolean exileIfCountered,
                                      DynamicAmount dynamicAmount, List<CardEffect> onNotPaidEffects,
                                      int lifeCost)
        implements CounterSpellingEffect, CounterUnlessEffect {

    /** Fixed-amount counter-unless-pays (e.g. Mana Leak). */
    public CounterUnlessPaysEffect(int amount) {
        this(amount, false, false, null, List.of(), 0);
    }

    /** Counter unless pays {@code amount} mana and {@code lifeCost} life (Mundungu). */
    public CounterUnlessPaysEffect(int amount, int lifeCost) {
        this(amount, false, false, null, List.of(), lifeCost);
    }

    public CounterUnlessPaysEffect(int amount, boolean useXValue, boolean exileIfCountered) {
        this(amount, useXValue, exileIfCountered, null, List.of(), 0);
    }

    /** Dynamic-amount counter-unless-pays ("{1} for each …", Spell Syphon). */
    public CounterUnlessPaysEffect(DynamicAmount dynamicAmount) {
        this(0, false, false, dynamicAmount, List.of(), 0);
    }

    /**
     * Counter-unless-pays with a not-paid rider (Power Sink): if the controller doesn't pay,
     * {@code onNotPaidEffects} resolve against them.
     */
    public CounterUnlessPaysEffect(int amount, boolean useXValue, boolean exileIfCountered,
                                   List<CardEffect> onNotPaidEffects) {
        this(amount, useXValue, exileIfCountered, null, onNotPaidEffects, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.SPELL_ON_STACK);
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.PAY_MANA;
    }

    @Override
    public int ransomMagnitude() {
        return amount;
    }
}
