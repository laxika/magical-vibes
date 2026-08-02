package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
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
            case OWN_ALL_MATCHING -> resolveOwnAllMatching(gameData, entry, e);
            case TARGET_CARDS_ANY_GRAVEYARD -> resolveTargetAnyGraveyardCards(gameData, entry, e);
            case TARGET_CARDS_OPPONENT_GRAVEYARD -> resolveTargetOpponentCards(gameData, entry);
            case TARGET_PLAYER_ENTIRE -> resolveTargetPlayerEntire(gameData, entry);
            case ALL_PLAYERS -> resolveAllGraveyards(gameData, entry);
            case ALL_OPPONENTS -> resolveAllOpponentsGraveyards(gameData, entry);
            case EACH_OPPONENT_KEEP -> resolveEachOpponentKeep(gameData, entry, e);
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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no graveyard cards to exile", gameData.id, playerName);
            return;
        }

        if (graveyard.size() <= count) {
            // Auto-exile all cards
            List<Card> toExile = new ArrayList<>(graveyard);
            graveyard.clear();
            graveyardService.notifyCardsLeftGraveyard(gameData, affectedPlayerId);
            for (Card card : toExile) {
                exileService.exileCard(gameData, affectedPlayerId, card);
            }
            GameLog.Builder builder = GameLog.builder().text(playerName + " exiles ");
            appendCardList(builder, toExile);
            builder.text(" from their graveyard.");
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} auto-exiles {} cards from graveyard", gameData.id, playerName, toExile.size());
        } else {
            // Player must choose which cards to exile
            graveyardReturnSupport.beginGraveyardExileChoice(gameData, affectedPlayerId, count);
        }
    }

    private void resolveOwnAllMatching(GameData gameData, StackEntry entry, ExileGraveyardCardsEffect e) {
        UUID playerId = e.affectedPlayerId() != null ? e.affectedPlayerId() : entry.getControllerId();
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards in graveyard to exile."));
            return;
        }

        List<Card> toExile = new ArrayList<>();
        for (Card card : graveyard) {
            if (e.filter() == null || predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)) {
                toExile.add(card);
            }
        }

        if (toExile.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards in graveyard to exile."));
            return;
        }

        graveyard.removeAll(toExile);
        graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
        for (Card card : toExile) {
            exileService.exileCard(gameData, playerId, card);
        }

        GameLog.Builder builder = GameLog.builder().text(playerName + " exiles ");
        appendCardList(builder, toExile);
        builder.text(" from their graveyard.");
        gameLogService.append(gameData, builder.build());
        log.info("Game {} - {} exiles {} matching cards from graveyard", gameData.id, playerName, toExile.size());
    }

    private void resolveEachOpponentKeep(GameData gameData, StackEntry entry, ExileGraveyardCardsEffect e) {
        UUID controllerId = entry.getControllerId();
        int keepCount = e.count();

        // Two-player engine: exactly one opponent. Each opponent keeps `keepCount` cards of their
        // choice in their graveyard and exiles the rest. Choosing which to exile is the exact
        // complement of choosing which to keep, so this reuses the standard graveyard-exile choice.
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            int size = graveyard == null ? 0 : graveyard.size();

            if (size <= keepCount) {
                String logEntry = playerName + " keeps their graveyard (" + size + " card"
                        + (size != 1 ? "s" : "") + "); nothing is exiled.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} keeps {} graveyard card(s); nothing exiled (each-opponent-keep)",
                        gameData.id, playerName, size);
                continue;
            }

            graveyardReturnSupport.beginGraveyardExileChoice(gameData, playerId, size - keepCount);
        }
    }

    /**
     * "Exile target card from a graveyard" ({@code count == 1}, single {@code targetId}) and its
     * multi-target flavour "exile up to N target cards from a single graveyard" ({@code count > 1},
     * targets riding in on {@code targetCardIds} — Rag Dealer). Each target that is no longer in a
     * graveyard, or no longer matches the filter, is simply skipped; a single-target effect with an
     * illegal target therefore still logs the fizzle it always did.
     */
    private void resolveTargetAnyGraveyardCards(GameData gameData, StackEntry entry, ExileGraveyardCardsEffect e) {
        List<UUID> targetCardIds = new ArrayList<>();
        if (entry.getTargetId() != null) {
            targetCardIds.add(entry.getTargetId());
        } else if (entry.getTargetCardIds() != null) {
            targetCardIds.addAll(entry.getTargetCardIds());
        }
        if (targetCardIds.isEmpty()) {
            return;
        }

        List<Card> exiledCards = new ArrayList<>();
        for (UUID targetCardId : targetCardIds) {
            Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (targetCard == null) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
                continue;
            }

            if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), null)) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target is no longer a valid "
                                + CardPredicateUtils.describeFilter(e.filter()) + ")."));
                continue;
            }

            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());

            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

            // Add to graveyard owner's exiled cards
            if (graveyardOwnerId != null) {
                exileService.exileCard(gameData, graveyardOwnerId, targetCard);
            }
            exiledCards.add(targetCard);
        }

        if (exiledCards.isEmpty()) {
            return;
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        GameLog.Builder builder = GameLog.builder().text(playerName + " exiles ");
        appendCardList(builder, exiledCards);
        builder.text(" from a graveyard.");
        gameLogService.append(gameData, builder.build());
    }

    private void resolveTargetOpponentCards(GameData gameData, StackEntry entry) {
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(entry.getControllerId());

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (no targets)."));
            return;
        }

        List<Card> exiledCards = new ArrayList<>();
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                exiledCards.add(card);
                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
            }
        }

        if (!exiledCards.isEmpty()) {
            GameLog.Builder builder = GameLog.builder().text(playerName + " exiles ");
            appendCardList(builder, exiledCards);
            builder.text(" from an opponent's graveyard.");
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} exiled {} cards from opponent's graveyard",
                    gameData.id, playerName, exiledCards.size());
        }
    }

    private void resolveTargetPlayerEntire(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is already empty.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        int count = graveyard.size();
        for (Card card : graveyard) {
            gameData.addToExile(targetPlayerId, card);
        }
        graveyard.clear();
        graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);

        String logEntry = playerName + "'s graveyard is exiled (" + count + " card" + (count != 1 ? "s" : "") + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));

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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - All graveyards exiled ({} cards) by {}",
                    gameData.id, totalExiled, entry.getCard().getName());
        } else {
            String logEntry = "All graveyards are already empty.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - All graveyards already empty when {} resolved",
                    gameData.id, entry.getCard().getName());
        }
    }

    /** Appends {@code cards} to {@code builder} as comma-separated card segments. */
    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {}'s graveyard ({} cards) exiled by ExileGraveyardCardsEffect(ALL_OPPONENTS)",
                    gameData.id, playerName, count);
        }
    }
}
