package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsOrLosesLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the opponent-only discard-or-life-loss flow. */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsOrLosesLifeEffectHandler implements NormalEffectHandlerBean {

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

        while (!state.remaining.isEmpty()) {
            UUID opponentId = state.remaining.removeFirst();
            state.currentPlayerId = opponentId;
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()
                    || gameQueryService.isDiscardPrevented(gameData, opponentId)) {
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
