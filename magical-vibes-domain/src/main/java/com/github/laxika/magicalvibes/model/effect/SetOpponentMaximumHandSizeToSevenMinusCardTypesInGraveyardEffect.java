package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * While the controller has delirium, sets each opponent's maximum hand size to seven minus the
 * number of distinct card types among cards in the controller's graveyard.
 */
public record SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect()
        implements OpponentMaxHandSizeEffect {

    private static final int DELIRIUM_THRESHOLD = 4;
    private static final int DEFAULT_MAXIMUM_HAND_SIZE = 7;

    @Override
    public int applyToMaximumHandSize(int currentMax) {
        return currentMax;
    }

    @Override
    public int applyToMaximumHandSize(int currentMax, GameData gameData, UUID sourceControllerId) {
        if (gameData == null || sourceControllerId == null) {
            return currentMax;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(sourceControllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return currentMax;
        }

        Set<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        for (Card card : graveyard) {
            if (card.isToken()) {
                continue;
            }
            if (card.getType() != null) {
                cardTypes.add(card.getType());
            }
            cardTypes.addAll(card.getAdditionalTypes());
        }

        return cardTypes.size() >= DELIRIUM_THRESHOLD
                ? DEFAULT_MAXIMUM_HAND_SIZE - cardTypes.size()
                : currentMax;
    }
}
