package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the {@code ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE} slot (Toxin Sliver).
 *
 * <p>"Whenever a [creature matching {@code sourceFilter}] deals combat damage to a creature, destroy
 * that creature." Unlike {@link DestroyDamagedCreatureEffect} the damage source may be any matching
 * creature, controlled by any player, and the ability is controlled by the watcher's controller.
 * The watcher is itself one of the possible sources. The ability belongs to the watcher, so a
 * matching creature that has lost its own abilities still triggers it.
 *
 * <p>Expanded at trigger-collection time into a {@link DestroyTargetPermanentEffect} stack entry
 * whose non-targeting target is the damaged creature, so the marker itself is never resolved.
 *
 * @param sourceFilter        which damage-dealing creatures the watcher cares about; {@code null} for any
 * @param combatDamageOnly    whether only combat damage counts
 * @param cannotBeRegenerated whether the destroyed creature can't be regenerated
 */
public record DestroyCreatureDamagedByMatchingCreatureEffect(PermanentPredicate sourceFilter,
                                                              boolean combatDamageOnly,
                                                              boolean cannotBeRegenerated)
        implements CardEffect {
}
