package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared helpers for "exile the top [type] card of your graveyard". The graveyard list is
 * append-ordered, so the top is its last element; a {@code requiredType} filter walks upward from
 * there and returns the first matching card, skipping nonmatching cards above it.
 *
 * <p>Used by the {@code ExileTopCardOfGraveyardCost} side of {@code ForcedCostOrElseEffect}
 * (Barrow Ghoul).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardTopExileSupport {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    /** The topmost card of {@code playerId}'s graveyard matching {@code requiredType}, or null. */
    public Card findTopMatching(GameData gameData, UUID playerId, CardType requiredType) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return null;
        }
        for (int i = graveyard.size() - 1; i >= 0; i--) {
            Card card = graveyard.get(i);
            if (requiredType == null || card.hasType(requiredType)) {
                return card;
            }
        }
        return null;
    }

    /** Exiles the topmost matching card; returns false when there is none. */
    public boolean exileTopMatching(GameData gameData, UUID playerId, CardType requiredType) {
        Card card = findTopMatching(gameData, playerId, requiredType);
        if (card == null) {
            return false;
        }
        gameData.playerGraveyards.get(playerId).remove(card);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, card);
        exileService.exileCard(gameData, playerId, card);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " exiles ", card, " from their graveyard."));
        log.info("Game {} - {} exiles {} from the top of their graveyard", gameData.id, playerName,
                card.getName());
        return true;
    }

    /** Returns the indices of all cards matching an arbitrary-card graveyard exile cost. */
    public List<Integer> matchingIndices(GameData gameData, UUID playerId, ExileCardFromGraveyardCost cost) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return List.of();
        }
        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            boolean typeMatches = cost.requiredType() == null
                    || card.hasType(cost.requiredType())
                    || (cost.alternateType() != null && card.hasType(cost.alternateType()));
            boolean subtypeMatches = cost.requiredSubtype() == null
                    || card.getSubtypes().contains(cost.requiredSubtype());
            if (typeMatches && subtypeMatches) {
                matchingIndices.add(i);
            }
        }
        return matchingIndices;
    }

    /** Exiles the sole matching arbitrary graveyard card; returns false when ambiguous or unavailable. */
    public boolean exileSoleMatching(GameData gameData, UUID playerId, ExileCardFromGraveyardCost cost) {
        List<Integer> matchingIndices = matchingIndices(gameData, playerId, cost);
        if (matchingIndices.size() != 1) {
            return false;
        }
        Card card = gameData.playerGraveyards.get(playerId).remove((int) matchingIndices.getFirst());
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, card);
        exileService.exileCard(gameData, playerId, card);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " exiles ", card, " from their graveyard."));
        log.info("Game {} - {} exiles {} from their graveyard", gameData.id, playerName, card.getName());
        return true;
    }
}
