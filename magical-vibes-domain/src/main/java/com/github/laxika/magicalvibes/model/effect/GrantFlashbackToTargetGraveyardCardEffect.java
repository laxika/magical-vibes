package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

import java.util.Set;

/**
 * Grants flashback to a single targeted card in the controller's graveyard
 * until end of turn. The flashback cost equals the card's mana cost unless
 * {@code withoutPayingManaCost} is true.
 * The target must match one of the specified card types.
 * (e.g. Snapcaster Mage — CR 702.33)
 */
public record GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes,
                                                        boolean withoutPayingManaCost) implements CardEffect {
    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes) {
        this(cardTypes, false);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)); }
}
