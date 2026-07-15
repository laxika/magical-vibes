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
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect} (Flux): in APNAP order,
 * each player chooses how many cards to discard (0 through their hand size), discards exactly
 * that many, then draws that many, before the next player takes their turn.
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

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

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
            state.remaining.addLast(gameData.activePlayerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.addLast(playerId);
                }
            }
            beginNextPlayer(gameData, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            // The current player just chose how many cards to discard.
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = state.currentPlayerId;
            String playerName = gameData.playerIdToName.get(playerId);

            if (chosenCount <= 0) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " discards 0 cards for " + cardName + "."));
                beginNextPlayer(gameData, cardName);
                return;
            }

            state.pendingDraw = chosenCount;
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            // Re-run this effect once the discard completes so we can draw and advance.
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, chosenCount, DiscardFollowUp.NONE);
            return;
        }

        // Re-entry after the current player's discard completed: draw that many, then advance.
        gameData.rerunCurrentEffectAfterInteraction = false;
        UUID playerId = state.currentPlayerId;
        int drawCount = state.pendingDraw;
        state.pendingDraw = 0;
        playerInteractionSupport.applyDrawCards(gameData, playerId, drawCount);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + "."));
        beginNextPlayer(gameData, cardName);
    }

    /**
     * Begins the next remaining player's X-value choice, skipping (with a log) any player whose
     * hand is empty. When no players remain, clears the flow so effect resolution can continue to
     * the spell's remaining effects (Flux's "Draw a card").
     */
    private void beginNextPlayer(GameData gameData, String cardName) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        while (!state.remaining.isEmpty()) {
            UUID nextPlayerId = state.remaining.pollFirst();
            List<Card> hand = gameData.playerHands.get(nextPlayerId);
            if (hand == null || hand.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(nextPlayerId) + " has no cards to discard for " + cardName + "."));
                continue;
            }
            state.currentPlayerId = nextPlayerId;
            String prompt = "Discard any number of cards for " + cardName
                    + ". You will draw that many cards.";
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.XValueChoice(nextPlayerId, hand.size(), prompt, cardName));
            return;
        }
        state.reset();
    }
}
