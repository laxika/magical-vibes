package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * "Whenever another creature you control enters, that creature gets +X/+Y and gains [keywords]
 * until end of turn" (Ogre Battledriver).
 *
 * <p>Trigger-materialising marker for {@code ON_ALLY_CREATURE_ENTERS_BATTLEFIELD}. Unlike a plain
 * {@link BoostTargetCreatureEffect} this does not target — "that creature" is the creature that
 * just entered. The enter collector resolves the entering permanent and puts a mandatory
 * {@code BoostTargetCreatureEffect} (plus a {@code GrantKeywordEffect(TARGET)} when
 * {@code keywords} is non-empty) onto the stack with {@code targetId} set to that creature and
 * {@code sourcePermanentId} set to this permanent.
 */
public record BoostEnteringCreatureEffect(int powerBoost, int toughnessBoost, Set<Keyword> keywords)
        implements CardEffect {

    /** Boost with no keyword grant. */
    public BoostEnteringCreatureEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of());
    }
}
