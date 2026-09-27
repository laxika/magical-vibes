package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForPlayerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawCardForPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardForPlayerEffect drawCard = (DrawCardForPlayerEffect) effect;
        if (gameData.playerIds.contains(drawCard.playerId())) {
            drawService.resolveDrawCard(gameData, drawCard.playerId());
        }
    }
}
