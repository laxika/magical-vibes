package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

import java.util.Set;

/**
 * Grants flashback to a single targeted card in the controller's graveyard
 * until end of turn. The flashback cost equals the card's mana cost unless
 * a fixed {@code flashbackCost} is supplied or {@code withoutPayingManaCost} is true.
 * The target must match one of the specified card types.
 * (e.g. Snapcaster Mage — CR 702.33)
 */
public record GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes,
                                                        boolean withoutPayingManaCost,
                                                        String flashbackCost) implements CardEffect {
    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes) {
        this(cardTypes, false, null);
    }

    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes, boolean withoutPayingManaCost) {
        this(cardTypes, withoutPayingManaCost, null);
    }

    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes, String flashbackCost) {
        this(cardTypes, false, flashbackCost);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)); }
}
