package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unified handler for {@link ExileGraveyardCardsEffect}, dispatching on
 * {@link com.github.laxika.magicalvibes.model.effect.GraveyardExileScope}. Each branch preserves the
 * behaviour (and log strings) of the record it was collapsed from.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileGraveyardCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileGraveyardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileGraveyardCardsEffect) effect;
        switch (e.scope()) {
            case OWN -> resolveOwn(gameData, entry, e);
            case TARGET_CARDS_ANY_GRAVEYARD -> resolveTargetSingleCard(gameData, entry, e);
            case TARGET_CARDS_OPPONENT_GRAVEYARD -> resolveTargetOpponentCards(gameData, entry);
            case TARGET_PLAYER_ENTIRE -> resolveTargetPlayerEntire(gameData, entry);
            case ALL_PLAYERS -> resolveAllGraveyards(gameData, entry);
            case ALL_OPPONENTS -> resolveAllOpponentsGraveyards(gameData, entry);
        }
    }

    private void resolveOwn(GameData gameData, StackEntry entry, ExileGraveyardCardsEffect e) {
        UUID affectedPlayerId = e.affectedPlayerId();
        if (affectedPlayerId == null) {
            affectedPlayerId = entry.getControllerId();
        }
        int count = e.count();
        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(affectedPlayerId);

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = playerName + " has no cards in graveyard to exile.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no graveyard cards to exile", gameData.id, playerName);
            return;
        }

        if (graveyard.size() <= count) {
            // Auto-exile all cards
            List<Card> toExile = new ArrayList<>(graveyard);
            graveyard.clear();
            graveyardService.notifyCardsLeftGraveyard(gameData, affectedPlayerId);
            List<String> exiledNames = new ArrayList<>();
            for (Card card : toExile) {
                exileService.exileCard(gameData, affectedPlayerId, card);
                exiledNames.add(card.getName());
            }
            String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} auto-exiles {} cards from graveyard", gameData.id, playerName, toExile.size());
        } else {
            // Player must choose which cards to exile
            graveyardReturnSupport.beginGraveyardExileChoice(gameData, affectedPlayerId, count);
        }
    }

    private void resolveTargetSingleCard(GameData gameData, StackEntry entry, ExileGraveyardCardsEffect e) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), null)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target is no longer a valid "
                            + CardPredicateUtils.describeFilter(e.filter()) + ")."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

        // Add to graveyard owner's exiled cards
        if (graveyardOwnerId != null) {
            exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + targetCard.getName() + " from a graveyard."));
    }

    private void resolveTargetOpponentCards(GameData gameData, StackEntry entry) {
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(entry.getControllerId());

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (no targets)."));
            return;
        }

        List<String> exiledNames = new ArrayList<>();
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                exiledNames.add(card.getName());
                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
            }
        }

        if (!exiledNames.isEmpty()) {
            String logEntry = playerName + " exiles " + String.join(", ", exiledNames)
                    + " from an opponent's graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} exiled {} cards from opponent's graveyard",
                    gameData.id, playerName, exiledNames.size());
        }
    }

    private void resolveTargetPlayerEntire(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is already empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        int count = graveyard.size();
        for (Card card : graveyard) {
            gameData.addToExile(targetPlayerId, card);
        }
        graveyard.clear();
        graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);

        String logEntry = playerName + "'s graveyard is exiled (" + count + " card" + (count != 1 ? "s" : "") + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {}'s graveyard ({} cards) exiled", gameData.id, playerName, count);
    }

    private void resolveAllGraveyards(GameData gameData, StackEntry entry) {
        int totalExiled = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) continue;
            for (Card card : graveyard) {
                exileService.exileCard(gameData, playerId, card);
                totalExiled++;
            }
            graveyard.clear();
            graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
        }

        if (totalExiled > 0) {
            String logEntry = "All graveyards are exiled (" + totalExiled + " card"
                    + (totalExiled != 1 ? "s" : "") + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - All graveyards exiled ({} cards) by {}",
                    gameData.id, totalExiled, entry.getCard().getName());
        } else {
            String logEntry = "All graveyards are already empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - All graveyards already empty when {} resolved",
                    gameData.id, entry.getCard().getName());
        }
    }

    private void resolveAllOpponentsGraveyards(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard.isEmpty()) continue;

            int count = graveyard.size();
            for (Card card : graveyard) {
                gameData.addToExile(playerId, card);
            }
            graveyard.clear();
            graveyardService.notifyCardsLeftGraveyard(gameData, playerId);

            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + "'s graveyard is exiled (" + count + " card" + (count != 1 ? "s" : "") + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {}'s graveyard ({} cards) exiled by ExileGraveyardCardsEffect(ALL_OPPONENTS)",
                    gameData.id, playerName, count);
        }
    }
}
