package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static marker on a permanent that creates {@code WHILE_SOURCE_TAPPED} untap locks
 * ({@link DoesntUntapEffect#targetWhileSourceTapped()}): when this permanent becomes untapped or
 * leaves the battlefield, every counter of the given type is removed from the permanents it locked,
 * and the locks themselves are released.
 *
 * <p>Giant Oyster's "When this creature leaves the battlefield or becomes untapped, remove all -1/-1
 * counters from the creature." Placed in {@code EffectSlot.STATIC} and read directly by
 * {@code UntapLockReleaseService} from the untap and leaves-the-battlefield paths — there is no
 * handler, the same way {@link DoesntUntapWithCounterEffect} is read by the untap step.
 *
 * @param counterType the kind of counter to remove from the locked permanents
 */
public record RemoveCountersWhenUntapLockEndsEffect(CounterType counterType) implements CardEffect {
}
