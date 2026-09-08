package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Exiles the targeted permanent, then has its controller reveal cards from their library until a
 * card matching one of the specified types is found. That card is put onto the battlefield under
 * that player's control, and all other revealed cards are shuffled into that library.
 */
public record ExileTargetThenRevealUntilTypeToBattlefieldEffect(
        Set<CardType> cardTypes
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
