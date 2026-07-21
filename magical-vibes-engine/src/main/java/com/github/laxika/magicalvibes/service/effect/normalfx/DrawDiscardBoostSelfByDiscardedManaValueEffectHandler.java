package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingBoostSourceByDiscardedManaValue;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardBoostSelfByDiscardedManaValueEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawDiscardBoostSelfByDiscardedManaValueEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawDiscardBoostSelfByDiscardedManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        // Draw a card
        drawService.resolveDrawCard(gameData, controllerId);
        // Set up the pending boost before the discard; applied once the player picks a card
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            gameData.pendingBoostSourceByDiscardedManaValue =
                    new PendingBoostSourceByDiscardedManaValue(sourcePermanentId);
        }
        // Then discard a card
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1);
    }
}
