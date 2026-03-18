package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Grants flashback to a single targeted card in the controller's graveyard
 * until end of turn. The flashback cost equals the card's mana cost.
 * The target must match one of the specified card types.
 * (e.g. Snapcaster Mage — CR 702.33)
 */
public record GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes) implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
}
