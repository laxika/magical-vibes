package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForMonarchEndStepEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DrawCardForMonarchEndStepEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    public DrawCardForMonarchEndStepEffectHandler(PlayerInteractionSupport playerInteractionSupport) {
        this.playerInteractionSupport = playerInteractionSupport;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForMonarchEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        if (playerId != null && gameData.playerIds.contains(playerId)) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        }
    }
}
