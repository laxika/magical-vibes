package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the {@code ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE} slot (Sosuke, Son of
 * Seshiro).
 *
 * <p>"Whenever a [creature matching {@code sourceFilter}] you control deals combat damage to a
 * creature, destroy that creature at end of combat." Unlike
 * {@link ReflectAllyDamageToDamagedCreatureControllerEffect} this only fires on <em>combat</em>
 * damage, and unlike {@link DestroyDamagedCreatureEffect} the damage source may be any matching
 * creature the watcher's controller controls, not just the watcher itself.
 *
 * <p>Expanded at trigger-collection time into a
 * {@link DestroyTargetPermanentAtEndOfCombatEffect} stack entry whose non-targeting target is the
 * damaged creature, so the marker itself is never resolved.
 *
 * <p>The flags also support the older "whenever this creature deals damage" wording: use
 * {@link #thisCreature()} for a self-scoped trigger that accepts both combat and noncombat damage.
 */
public record DestroyDamagedCreatureAtEndOfCombatEffect(PermanentPredicate sourceFilter,
                                                        boolean combatDamageOnly,
                                                        boolean selfOnly) implements CardEffect {

    public DestroyDamagedCreatureAtEndOfCombatEffect(PermanentPredicate sourceFilter) {
        this(sourceFilter, true, false);
    }

    public static DestroyDamagedCreatureAtEndOfCombatEffect thisCreature() {
        return new DestroyDamagedCreatureAtEndOfCombatEffect(null, false, true);
    }
}
