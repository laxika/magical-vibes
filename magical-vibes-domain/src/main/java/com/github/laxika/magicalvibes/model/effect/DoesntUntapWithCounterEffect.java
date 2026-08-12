package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Continuous static effect placed in {@code EffectSlot.STATIC}: the source permanent doesn't untap
 * during its controller's untap step for as long as it has at least one counter of the given type on
 * it. The counter-conditional sibling of {@link DoesntUntapEffect#self()} (which never untaps at
 * all), read directly by {@code UntapStepService} rather than through a handler.
 *
 * <p>Used by the Ice Age depletion lands ("This land doesn't untap during your untap step if it has
 * a depletion counter on it") — e.g. Land Cap. Also honoured when granted rather than printed:
 * {@code UntapStepService} reads the permanent's persistent granted STATIC effects too, which is how
 * fungus counters (Mindbender Spores) lock the blocked creature.
 *
 * @param counterType the counter whose presence keeps the permanent tapped
 */
public record DoesntUntapWithCounterEffect(CounterType counterType, TapUntapScope scope) implements CardEffect {

    public DoesntUntapWithCounterEffect(CounterType counterType) {
        this(counterType, TapUntapScope.SELF);
    }

    public static DoesntUntapWithCounterEffect enchanted(CounterType counterType) {
        return new DoesntUntapWithCounterEffect(counterType, TapUntapScope.ENCHANTED);
    }
}
