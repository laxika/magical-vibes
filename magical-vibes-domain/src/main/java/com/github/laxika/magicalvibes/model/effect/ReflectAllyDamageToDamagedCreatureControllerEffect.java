package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the {@code ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE} slot (Greatbow Doyen).
 *
 * <p>"Whenever a [creature matching {@code sourceFilter}] you control deals damage to a creature,
 * that creature deals that much damage to that creature's controller." The trigger collector
 * translates each match into a {@link DealDamageToPlayersEffect} dealt by the damage-source
 * creature to the damaged creature's controller — the marker itself is never resolved.
 */
public record ReflectAllyDamageToDamagedCreatureControllerEffect(PermanentPredicate sourceFilter) implements CardEffect {
}
