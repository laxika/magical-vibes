package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static "can't untap more than N [permanents] during untap steps" lock (Static Orb, Stoic Angel).
 *
 * <p>While an active copy of this effect is present, each player's untap step pauses to let the
 * active player choose up to {@code maxUntap} of the permanents that would otherwise untap and that
 * match {@code filter}; only those (plus any permanents the filter excludes) untap that step.
 *
 * @param maxUntap              cap on how many matching permanents may untap (2 for Static Orb, 1 for
 *                              Stoic Angel)
 * @param filter               which permanents the cap applies to; {@code null} means all permanents
 *                              (Static Orb). Permanents excluded by the filter untap normally.
 * @param requiresUntappedSource {@code true} if the lock only applies while its source is untapped
 *                              (Static Orb: "As long as Static Orb is untapped…"); {@code false} if it
 *                              applies whenever the source is on the battlefield (Stoic Angel).
 */
public record StaticOrbEffect(int maxUntap, PermanentPredicate filter, boolean requiresUntappedSource)
        implements CardEffect {

    /** Static Orb: cap of two on all permanents, active only while the source is untapped. */
    public StaticOrbEffect() {
        this(2, null, true);
    }
}
