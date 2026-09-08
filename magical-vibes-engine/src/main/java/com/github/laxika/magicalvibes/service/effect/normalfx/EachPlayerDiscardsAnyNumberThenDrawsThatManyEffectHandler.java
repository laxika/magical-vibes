package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.springframework.beans.factory.ObjectProvider;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect} (Flux): in APNAP order,
 * each player chooses how many cards to discard (0 through their hand size) and selects them.
 * After all choices, the selected cards are discarded and each player draws their discarded count.
 *
 * <p>The flow is driven one player at a time and re-runs on every interaction completion. Each
 * player's turn is a two-phase interaction: an {@link PendingInteraction.XValueChoice} for the
 * count, then a discard selection. The X-value answer re-runs this handler (the engine re-runs
 * the current effect while an X-value choice is active); the discard completion re-runs it via
 * {@code rerunCurrentEffectAfterInteraction}, set while the discard is outstanding. Progress
 * lives on {@link GameData#eachPlayerRummage}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsAnyNumberThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final ObjectProvider<CardChoiceHandlerService> cardChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            // Fresh entry: seed the APNAP queue and begin the first player's choice.
            state.active = true;
            state.pendingDraw = 0;
            state.currentPlayerId = null;
            state.remaining.clear();
            state.deferDiscards = true;
            state.selectedDiscards.clear();
            state.remaining.addLast(gameData.activePlayerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.addLast(playerId);
                }
            }
            beginNextPlayer(gameData, entry, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            // The current player just chose how many cards to discard.
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = state.currentPlayerId;
            String playerName = gameData.playerIdToName.get(playerId);

            if (chosenCount <= 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " discards 0 cards for " + cardName + "."));
                beginNextPlayer(gameData, entry, cardName);
                return;
            }

            if (!gameQueryService.canEffectCauseDiscard(gameData, playerId, entry.getControllerId())) {
                beginNextPlayer(gameData, entry, cardName);
                return;
            }
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            // Re-run this effect once selection completes to advance to the next player.
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, chosenCount, DiscardFollowUp.NONE);
            return;
        }

        // Re-entry after selection: advance without revealing or moving the chosen cards.
        gameData.rerunCurrentEffectAfterInteraction = false;
        beginNextPlayer(gameData, entry, cardName);
    }

    /**
     * Begins the next remaining player's X-value choice, skipping (with a log) any player whose
     * hand is empty. When no players remain, clears the flow so effect resolution can continue to
     * the spell's remaining effects (Flux's "Draw a card").
     */
    private void beginNextPlayer(GameData gameData, StackEntry entry, String cardName) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        while (!state.remaining.isEmpty()) {
            UUID nextPlayerId = state.remaining.pollFirst();
            List<Card> hand = gameData.playerHands.get(nextPlayerId);
            if (hand == null || hand.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(nextPlayerId) + " has no cards to discard for " + cardName + "."));
                continue;
            }
            state.currentPlayerId = nextPlayerId;
            String prompt = "Discard any number of cards for " + cardName
                    + ". You will draw that many cards.";
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.XValueChoice(nextPlayerId, hand.size(), prompt, cardName));
            return;
        }
        var discardedCounts = cardChoiceHandlerServiceProvider.getObject().discardCollectedCards(
                gameData, List.copyOf(state.selectedDiscards), entry.getControllerId());
        state.reset();
        for (UUID playerId : gameData.orderedPlayerIds.stream()
                .sorted(java.util.Comparator.comparing(id -> !id.equals(gameData.activePlayerId))).toList()) {
            int drawCount = discardedCounts.getOrDefault(playerId, 0);
            if (drawCount > 0) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, drawCount);
            }
        }
    }
}
