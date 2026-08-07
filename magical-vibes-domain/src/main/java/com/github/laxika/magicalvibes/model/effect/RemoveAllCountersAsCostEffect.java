package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Additional cost: "Remove all {counterType} counters from this permanent." The number of
 * counters removed this way is snapshotted into the activated ability's xValue (in
 * {@code ActivatedAbilityExecutionService}) before the counters are cleared, so an
 * accompanying effect (e.g. {@link LookAtTopCardsEffect} with a {@code XValue()} look count)
 * can scale with the number of counters removed. Used by Jar of Eyeballs ({@code CounterType.EYEBALL}).
 *
 * <p>With {@code fromGrantingEquipment = true} the counters are removed from the Equipment that
 * granted the ability instead of from the permanent activating it — Hankyu's "{T}, Remove all aim
 * counters from Hankyu" is activated by the equipped creature but the counters sit on the
 * Equipment.</p>
 */
public record RemoveAllCountersAsCostEffect(CounterType counterType, boolean fromGrantingEquipment)
        implements CostEffect {

    public RemoveAllCountersAsCostEffect(CounterType counterType) {
        this(counterType, false);
    }
}
