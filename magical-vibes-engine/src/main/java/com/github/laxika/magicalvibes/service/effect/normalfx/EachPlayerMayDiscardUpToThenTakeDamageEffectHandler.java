package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardUpToThenTakeDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerMayDiscardUpToThenTakeDamageEffect} (Mind Bomb): in APNAP order,
 * each player may discard up to {@code amount} cards, then is dealt {@code amount} minus the
 * number of cards they discarded this way.
 *
 * <p>The flow is driven one player at a time and re-runs on every interaction completion, mirroring
 * {@link EachPlayerDiscardsAnyNumberThenDrawsThatManyEffectHandler}. Each player's turn is an
 * {@link PendingInteraction.XValueChoice} for the discard count (capped at {@code min(amount, hand
 * size)}); the chosen count fixes the damage ({@code amount - chosen}), which is dealt immediately.
 * A non-zero choice then runs the discard selection, re-running this handler afterwards (via
 * {@code rerunCurrentEffectAfterInteraction}) to advance. Progress reuses {@link GameData#eachPlayerRummage}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardUpToThenTakeDamageEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardUpToThenTakeDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int amount = ((EachPlayerMayDiscardUpToThenTakeDamageEffect) effect).amount();
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
            beginNextPlayer(gameData, entry, amount, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            // The current player just chose how many cards to discard.
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = state.currentPlayerId;
            dealDamage(gameData, entry, playerId, amount - chosenCount);

            if (chosenCount <= 0) {
                beginNextPlayer(gameData, entry, amount, cardName);
                return;
            }

            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            // Re-run this effect once the discard completes so we can advance.
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, chosenCount, DiscardFollowUp.NONE);
            return;
        }

        // Re-entry after the current player's discard completed: advance to the next player.
        gameData.rerunCurrentEffectAfterInteraction = false;
        beginNextPlayer(gameData, entry, amount, cardName);
    }

    /**
     * Begins the next remaining player's discard choice. A player who cannot discard anything
     * ({@code min(amount, hand size) == 0}) is dealt the full {@code amount} immediately and skipped.
     * When no players remain, clears the flow so resolution can continue.
     */
    private void beginNextPlayer(GameData gameData, StackEntry entry, int amount, String cardName) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        while (!state.remaining.isEmpty()) {
            UUID nextPlayerId = state.remaining.pollFirst();
            state.currentPlayerId = nextPlayerId;
            List<Card> hand = gameData.playerHands.get(nextPlayerId);
            int handSize = hand == null ? 0 : hand.size();
            int maxDiscard = Math.min(amount, handSize);
            if (maxDiscard <= 0) {
                dealDamage(gameData, entry, nextPlayerId, amount);
                continue;
            }
            String prompt = "Discard up to " + maxDiscard + " card" + (maxDiscard != 1 ? "s" : "")
                    + " for " + cardName + ". You will be dealt " + amount + " damage minus the number discarded.";
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.XValueChoice(nextPlayerId, maxDiscard, prompt, cardName));
            return;
        }
        state.reset();
    }

    private void dealDamage(GameData gameData, StackEntry entry, UUID playerId, int amount) {
        if (amount <= 0) return;
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
