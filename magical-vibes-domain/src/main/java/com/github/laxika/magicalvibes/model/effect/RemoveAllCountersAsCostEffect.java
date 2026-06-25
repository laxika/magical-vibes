package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Additional cost: "Remove all {counterType} counters from this permanent." The number of
 * counters removed this way is snapshotted into the activated ability's xValue (in
 * {@code ActivatedAbilityExecutionService}) before the counters are cleared, so an
 * accompanying effect (e.g. {@link LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect})
 * can scale with the number of counters removed. Used by Jar of Eyeballs ({@code CounterType.EYEBALL}).
 */
public record RemoveAllCountersAsCostEffect(CounterType counterType) implements CostEffect {
}
