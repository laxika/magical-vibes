package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingTransformOnCreatureDiscard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawDiscardTransformIfCreatureDiscardedEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawDiscardTransformIfCreatureDiscardedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawDiscardTransformIfCreatureDiscardedEffect) effect;

        UUID controllerId = entry.getControllerId();
        // Draw a card
        drawService.resolveDrawCard(gameData, controllerId);
        // Set up pending conditional transform before the discard
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            gameData.pendingTransformOnCreatureDiscard = new PendingTransformOnCreatureDiscard(sourcePermanentId);
        }
        // Then discard a card
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1);
    
    }
}
