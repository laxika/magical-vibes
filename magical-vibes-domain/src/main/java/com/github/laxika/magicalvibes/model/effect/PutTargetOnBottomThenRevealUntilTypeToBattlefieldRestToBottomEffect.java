package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Puts the targeted creature on the bottom of its owner's library, then has that creature's
 * controller reveal cards from their library until a card matching one of the specified types is
 * revealed. The matching card is put onto the battlefield under that player's control, and the
 * other revealed cards are put on the bottom of that library in any order.
 */
public record PutTargetOnBottomThenRevealUntilTypeToBattlefieldRestToBottomEffect(
        Set<CardType> cardTypes
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
