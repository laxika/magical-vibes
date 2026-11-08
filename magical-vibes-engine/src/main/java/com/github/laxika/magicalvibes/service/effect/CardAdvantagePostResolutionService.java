package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Judges "a spell you control causes you to gain card advantage" (Toofer, Keeper of the Full Grip): you gain card
 * advantage each time a spell you control resolves and you end up having more cards relative to your opponents than
 * before that spell resolved. A player's cards are the cards in their hand and their nontoken permanents; a spell cast
 * from its owner's hand still counts as one of that player's cards before it resolves.
 */
public final class CardAdvantagePostResolutionService {

    private CardAdvantagePostResolutionService() {
    }

    /**
     * Records each player's card count on {@code spellEntry} as the spell begins to resolve, so changes made by spells
     * that resolved after it was cast don't count toward it. Abilities don't cause card advantage this way, so an
     * ability's entry records none.
     */
    public static void snapshotBeforeResolution(GameData gameData, StackEntry spellEntry) {
        if (spellEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || spellEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return;
        }
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID playerId : gameData.playerIds) {
            counts.put(playerId, cardResourceCount(gameData, playerId));
        }
        Card spellCard = spellEntry.getCard();
        if (spellCard != null && !spellEntry.isCopy() && spellEntry.getOwnerId() != null
                && gameData.wasSpellCastFromHandThisTurn(spellCard.getId())) {
            counts.merge(spellEntry.getOwnerId(), 1, Integer::sum);
        }
        spellEntry.setCardCountsBeforeResolution(counts);
    }

    /**
     * Whether {@code spellEntry}, which just finished resolving, gave its controller card advantage: their gain in cards
     * since it began to resolve is greater than their opponents' gain together. Clears the counts the spell recorded,
     * so a spell is judged once; {@code false} for an entry that recorded none.
     */
    public static boolean causedCardAdvantage(GameData gameData, StackEntry spellEntry) {
        Map<UUID, Integer> before = spellEntry.takeCardCountsBeforeResolution();
        UUID controllerId = spellEntry.getControllerId();
        if (before == null || controllerId == null) {
            return false;
        }
        int controllerDelta = cardResourceCount(gameData, controllerId) - before.getOrDefault(controllerId, 0);
        int opponentsDelta = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .mapToInt(playerId -> cardResourceCount(gameData, playerId) - before.getOrDefault(playerId, 0))
                .sum();
        return controllerDelta > opponentsDelta;
    }

    private static int cardResourceCount(GameData gameData, UUID playerId) {
        int handCards = (int) gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .filter(card -> isCard(gameData, card))
                .count();
        int battlefieldCards = (int) gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> isCard(gameData, permanent.getCard()))
                .count();
        return handCards + battlefieldCards;
    }

    private static boolean isCard(GameData gameData, Card card) {
        return card != null && !card.isToken() && !gameData.dynamicTokenCardIds.contains(card.getId());
    }
}
