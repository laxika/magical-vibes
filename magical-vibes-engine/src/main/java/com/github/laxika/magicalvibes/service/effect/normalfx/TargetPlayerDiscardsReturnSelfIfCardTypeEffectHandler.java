package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsReturnSelfIfCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsReturnSelfIfCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDiscardsReturnSelfIfCardTypeEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        if (targetHand != null && !targetHand.isEmpty()) {
            gameData.pendingReturnToHandOnDiscardType = new PendingReturnToHandOnDiscardType(
                    entry.getCard(), entry.getControllerId(), e.returnIfType());
        }
        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, e.amount());
    
    }
}
