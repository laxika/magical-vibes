package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Destroys the targeted permanent (optionally preventing regeneration), then reveals cards from
 * the top of its controller's library until a card matching one of the specified types is found.
 * That card is put onto the battlefield under that player's control, and all other revealed cards
 * are shuffled into their library.
 * <p>
 * The reveal happens regardless of whether the destruction succeeds (e.g. indestructible).
 * Used by Polymorph (creature → creature with cannotBeRegenerated=true).
 */
public record DestroyTargetThenRevealUntilTypeToBattlefieldEffect(
        boolean cannotBeRegenerated,
        Set<CardType> cardTypes
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
