package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CaitCageBrawlerState;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CaitCageBrawlerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Cait's draw/discard comparison, preserving both choices across input pauses. */
@Component
@RequiredArgsConstructor
public class CaitCageBrawlerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CaitCageBrawlerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CaitCageBrawlerState state = gameData.caitCageBrawler;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.defendingPlayerId = defendingPlayerId(gameData, entry);
            state.remaining.add(state.controllerId);
            if (state.defendingPlayerId != null
                    && !state.defendingPlayerId.equals(state.controllerId)) {
                state.remaining.add(state.defendingPlayerId);
            }

            // Both players draw before either discard is chosen.
            for (UUID playerId : List.copyOf(state.remaining)) {
                drawService.resolveDrawCard(gameData, playerId);
            }
        } else if (state.currentPlayerId != null) {
            state.discardedManaValues.put(state.currentPlayerId,
                    gameData.lastDiscardedCardManaValue);
            state.currentPlayerId = null;
        }

        processNextDiscard(gameData, entry, state);
    }

    private void processNextDiscard(GameData gameData, StackEntry entry,
            CaitCageBrawlerState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            boolean opponentDiscard = !playerId.equals(state.controllerId);
            if (hand == null || hand.isEmpty()
                    || opponentDiscard && gameQueryService.isDiscardPrevented(gameData, playerId)) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.lastDiscardedCardManaValue = 0;
            gameData.discardCausedByOpponent = opponentDiscard;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            state.discardedManaValues.put(playerId, gameData.lastDiscardedCardManaValue);
            state.currentPlayerId = null;
        }

        finish(gameData, entry, state);
    }

    private void finish(GameData gameData, StackEntry entry, CaitCageBrawlerState state) {
        Integer controllerDiscard = state.discardedManaValues.get(state.controllerId);
        Integer defendingDiscard = state.defendingPlayerId == null
                ? null : state.discardedManaValues.get(state.defendingPlayerId);
        boolean controllerTiedForGreatest = controllerDiscard != null
                && (defendingDiscard == null || controllerDiscard >= defendingDiscard);

        if (controllerTiedForGreatest && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, source, 2);
            }
        }

        state.reset();
        gameData.discardCausedByOpponent = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        UUID attackedTargetId = entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId() : entry.getTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
