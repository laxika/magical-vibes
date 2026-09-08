package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Every player can't cast spells of the specified types until the end of the effect controller's
 * next turn.
 */
public record PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect(Set<CardType> restrictedTypes)
        implements CardEffect {

    public PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect {
        restrictedTypes = Set.copyOf(restrictedTypes);
    }
}
