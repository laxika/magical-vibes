package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect (ON_CONTROLLER_GAINS_LIFE): "Whenever you gain life, you may pay {manaCost}.
 * If you do, put a +1/+1 counter on target creature for each 1 life you gained." (Cradle of Vitality)
 *
 * <p>A bespoke marker consumed by {@code MiscTriggerCollectorService}: the trigger queues a
 * creature-only target choice carrying a {@link MayPayManaEffect} that wraps a
 * {@link PutCounterOnTargetPermanentEffect}, with the counter count locked in at trigger time as the
 * life gained by the event.</p>
 */
public record CradleOfVitalityLifeGainEffect(String manaCost) implements CardEffect {
}
