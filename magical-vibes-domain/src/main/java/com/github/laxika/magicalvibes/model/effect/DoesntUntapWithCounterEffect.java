package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Continuous static effect added as a static ability: the permanent {@code subject} names ({@code source()} or
 * {@code attached()}) doesn't untap during its controller's untap step for as long as the
 * {@code counterBearer} has at least one counter of the given type. The counter-conditional sibling
 * of {@link DoesntUntapEffect#self()} (which never untaps at all), read directly by
 * {@code UntapStepService} rather than through a handler.
 *
 * <p>Used by the Ice Age depletion lands ("This land doesn't untap during your untap step if it has
 * a depletion counter on it") — e.g. Land Cap. Also honoured when granted rather than printed:
 * {@code UntapStepService} reads the permanent's persistent granted STATIC effects too, which is how
 * fungus counters (Mindbender Spores) lock the blocked creature.
 *
 * @param counterType   the counter whose presence keeps the permanent tapped
 * @param subject       the permanent that is prevented from untapping
 * @param counterBearer the source or attached permanent whose counters control the lock
 */
public record DoesntUntapWithCounterEffect(
        CounterType counterType,
        PermanentSubject subject,
        PermanentSubject counterBearer
) implements CardEffect {

    public DoesntUntapWithCounterEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Attached)) {
            throw new IllegalArgumentException("A counter lock names source() or attached(), got " + subject);
        }
    }

    public DoesntUntapWithCounterEffect(CounterType counterType) {
        this(counterType, Subjects.source(), Subjects.source());
    }

    public static DoesntUntapWithCounterEffect enchanted(CounterType counterType) {
        return new DoesntUntapWithCounterEffect(
                counterType, Subjects.attached(), Subjects.source());
    }

    public static DoesntUntapWithCounterEffect enchantedWithCounterOnEnchantedPermanent(CounterType counterType) {
        return new DoesntUntapWithCounterEffect(
                counterType, Subjects.attached(), Subjects.attached());
    }
}
