package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerRandomDiscardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRandomDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerRandomDiscardEffect) effect;

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        // APNAP order: active player first, then others in turn order
        UUID activePlayerId = gameData.activePlayerId;
        gameData.discardCausedByOpponent = !activePlayerId.equals(controllerId);
        playerInteractionSupport.resolveRandomDiscardCards(gameData, activePlayerId, sourceName, e.amount());
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.discardCausedByOpponent = !playerId.equals(controllerId);
                playerInteractionSupport.resolveRandomDiscardCards(gameData, playerId, sourceName, e.amount());
            }
        }
    
    }
}
