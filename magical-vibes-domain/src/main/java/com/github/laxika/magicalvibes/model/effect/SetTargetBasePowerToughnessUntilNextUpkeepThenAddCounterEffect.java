package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentCastBySourceControllerThisTurnPredicate;

/**
 * Cycle of Life: "Target creature you cast this turn has base power and toughness 0/1 until your
 * next upkeep. At the beginning of your next upkeep, put a +1/+1 counter on that creature."
 *
 * <p>Resolution registers a layer-7b base-P/T floating effect with
 * {@link EffectDuration#UNTIL_CONTROLLERS_NEXT_UPKEEP} — a longer duration than the usual
 * {@link SetBasePowerToughnessEffect}, so the 0/1 survives end-of-turn cleanup — and queues a
 * {@code PutCounterOnPermanentAtNextUpkeep} delayed action for the same permanent. Both end at
 * the controller's next upkeep, in that order (the base P/T wears off, then the delayed trigger
 * adds the counter).
 *
 * @param power         the base power to set
 * @param toughness     the base toughness to set
 * @param counterType   the counter put on the creature at the controller's next upkeep
 * @param counterAmount how many of those counters to add
 */
public record SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect(
        int power, int toughness, CounterType counterType, int counterAmount) implements CardEffect {

    /** Cycle of Life's shape: base P/T then a single +1/+1 counter. */
    public SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect(int power, int toughness) {
        this(power, toughness, CounterType.PLUS_ONE_PLUS_ONE, 1);
    }

    /**
     * Structural spec only: the "you cast this turn" narrowing lives on the ability's
     * {@code TargetFilter}, which is the one validation path that knows who the source's
     * controller is ({@code TargetValidationContext} carries no controller, so a
     * {@link PermanentCastBySourceControllerThisTurnPredicate} here would never match).
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
