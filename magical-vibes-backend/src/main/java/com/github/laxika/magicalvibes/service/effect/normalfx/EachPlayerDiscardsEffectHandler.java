package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerDiscardsEffect) effect;

        UUID controllerId = entry.getControllerId();
        // Build APNAP-ordered queue: active player first, then others in turn order
        gameData.pendingEachPlayerDiscardQueue.clear();
        gameData.pendingEachPlayerDiscardControllerId = controllerId;
        UUID activePlayerId = gameData.activePlayerId;
        gameData.pendingEachPlayerDiscardQueue.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerDiscardQueue.add(playerId);
            }
        }
        // Store the amount for later queue processing
        gameData.pendingEachPlayerDiscardAmount = e.amount();
        // Start the first player's discard
        playerInteractionSupport.startNextEachPlayerDiscard(gameData);
    
    }
}
