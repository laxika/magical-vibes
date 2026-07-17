package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static effect: can't cast spells of the specified types. When {@code appliesToAllPlayers} is
 * false (default) only the source's controller is restricted (Steel Golem: "You can't cast
 * creature spells"); when true the restriction is symmetric across every player (Aether Storm:
 * "Creature spells can't be cast").
 */
public record CantCastSpellTypeEffect(Set<CardType> restrictedTypes, boolean appliesToAllPlayers)
        implements CardEffect {

    public CantCastSpellTypeEffect(Set<CardType> restrictedTypes) {
        this(restrictedTypes, false);
    }
}
