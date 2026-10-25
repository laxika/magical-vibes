package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsOrLosesLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the opponent-only discard-or-life-loss flow (Entropic Battlecruiser): opponents are taken in APNAP order,
 * one who can discard is asked to, one who can't loses the life when their turn comes, and the handler is resolved
 * again after each discard.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: each run changes nothing before its losses, and a discard prompt ends
 * it, so before anything changes the run asks, through {@link LifeSupport#askBeforeLifeLosses}, the Lich's Duel
 * Mastery sacrifices (CR 616.1) of the losses it reaches before the next prompt (the first loss closes the window).
 */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsOrLosesLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsOrLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentDiscardsOrLosesLifeEffect discardEffect =
                (EachOpponentDiscardsOrLosesLifeEffect) effect;
        EachPlayerDiscardsOrLosesLifeState state = gameData.eachPlayerDiscardsOrLosesLife;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(orderedOpponents(gameData, entry.getControllerId()));
        } else if (state.discardPending) {
            state.discardPending = false;
            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        Map<UUID, Integer> lossesBeforeNextPrompt = new LinkedHashMap<>();
        for (UUID opponentId : state.remaining) {
            if (!cantDiscard(gameData, opponentId)) {
                break;
            }
            lossesBeforeNextPrompt.put(opponentId, discardEffect.lifeLoss());
        }
        lifeSupport.askBeforeLifeLosses(gameData, lossesBeforeNextPrompt);

        while (!state.remaining.isEmpty()) {
            UUID opponentId = state.remaining.removeFirst();
            state.currentPlayerId = opponentId;
            if (cantDiscard(gameData, opponentId)) {
                lifeSupport.applyLifeLoss(gameData, opponentId, discardEffect.lifeLoss(),
                        entry.getCard().getName());
                continue;
            }

            state.discardPending = true;
            gameData.discardCausedByOpponent = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, opponentId, 1, DiscardFollowUp.NONE);
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private boolean cantDiscard(GameData gameData, UUID opponentId) {
        List<Card> hand = gameData.playerHands.get(opponentId);
        return hand == null || hand.isEmpty() || gameQueryService.isDiscardPrevented(gameData, opponentId);
    }

    private List<UUID> orderedOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
