package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * ON_ANY_PLAYER_TAPS_LAND trigger: "Whenever a player taps a land for mana, this deals {damage}
 * damage to that player." Symmetric — every player's land taps trigger it.
 *
 * <p>{@code landFilter} optionally narrows which tapped lands trigger it (evaluated against the
 * tapped land); {@code null} means every land. Manabarbs uses no filter; Burning Earth passes a
 * nonbasic-land predicate.
 */
public record DealDamageOnLandTapEffect(int damage, PermanentPredicate landFilter) implements CardEffect {

    public DealDamageOnLandTapEffect(int damage) {
        this(damage, null);
    }
}
