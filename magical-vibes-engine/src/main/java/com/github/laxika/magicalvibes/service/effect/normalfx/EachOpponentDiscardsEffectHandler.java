package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentDiscardsEffect) effect;

        UUID controllerId = entry.getControllerId();
        // Build APNAP-ordered queue with only opponents (skip controller)
        gameData.pendingEachPlayerDiscardQueue.clear();
        gameData.pendingEachPlayerDiscardControllerId = controllerId;
        UUID activePlayerId = gameData.activePlayerId;
        if (!activePlayerId.equals(controllerId)) {
            gameData.pendingEachPlayerDiscardQueue.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                gameData.pendingEachPlayerDiscardQueue.add(playerId);
            }
        }
        gameData.pendingEachPlayerDiscardAmount = e.amount();
        playerInteractionSupport.startNextEachPlayerDiscard(gameData);
    
    }
}
