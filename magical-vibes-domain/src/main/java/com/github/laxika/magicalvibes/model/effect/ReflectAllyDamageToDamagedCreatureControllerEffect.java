package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the {@code ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE} slot (Greatbow Doyen).
 *
 * <p>The normal form means "Whenever a [creature matching {@code sourceFilter}] you control deals
 * damage to a creature, that creature deals that much damage to that creature's controller." The
 * trigger collector translates each match into a {@link DealDamageToPlayersEffect} dealt by the
 * damage-source creature to the damaged creature's controller; the marker itself is never
 * resolved.
 *
 * <p>The self-scoped combat-life-loss form is used by Flayed Nim. It keeps the same trigger
 * collector path while changing the result to life loss and restricting it to combat damage.
 */
public record ReflectAllyDamageToDamagedCreatureControllerEffect(
        PermanentPredicate sourceFilter,
        boolean combatOnly,
        boolean sourceMustBeWatcher,
        boolean lifeLoss) implements CardEffect {

    public ReflectAllyDamageToDamagedCreatureControllerEffect(PermanentPredicate sourceFilter) {
        this(sourceFilter, false, false, false);
    }

    public static ReflectAllyDamageToDamagedCreatureControllerEffect selfCombatDamageCausesLifeLoss() {
        return new ReflectAllyDamageToDamagedCreatureControllerEffect(null, true, true, true);
    }
}
