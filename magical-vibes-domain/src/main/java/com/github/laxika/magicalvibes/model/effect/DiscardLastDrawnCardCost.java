package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Activation cost that can be paid only by discarding the last card the player drew this turn.
 * The card must still be in that player's hand when the cost is paid.
 */
public record DiscardLastDrawnCardCost() implements HandCardCost {

    @Override
    public CardPredicate predicate() {
        return null;
    }

    @Override
    public String label() {
        return null;
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public boolean isEligible(GameData gameData, UUID playerId, Card card) {
        List<UUID> drawnCardIds = gameData.cardsDrawnThisTurnIds.get(playerId);
        return drawnCardIds != null
                && !drawnCardIds.isEmpty()
                && drawnCardIds.get(drawnCardIds.size() - 1).equals(card.getId());
    }
}
