package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts {@code count} counters of the given type on every permanent the source permanent currently
 * holds a {@code WHILE_SOURCE_TAPPED} untap lock on (the permanents whose
 * {@code untapPreventedByPermanentIds} contain the source). Non-targeting: the affected permanents
 * are derived from the locks the source already created via
 * {@link DoesntUntapEffect#targetWhileSourceTapped()}.
 *
 * <p>Giant Oyster's "at the beginning of each of your draw steps, put a -1/-1 counter on that
 * creature" rider, placed in {@code EffectSlot.DRAW_TRIGGERED}. Because the lock only exists for as
 * long as the source stays tapped, the "for as long as this creature remains tapped" clause needs no
 * separate condition — an untapped source holds no locks and the trigger does nothing.
 *
 * <p>Pair with {@link RemoveCountersWhenUntapLockEndsEffect} when the counters must be removed again
 * once the lock ends.
 *
 * @param counterType the kind of counter to place
 * @param count       how many counters to place on each locked permanent
 */
public record PutCountersOnUntapLockedPermanentsEffect(CounterType counterType, int count) implements CardEffect {
}
