package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllButOneLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the Soulgorger Orgg-style "lose all but 1 life" effect. */
@Component
@RequiredArgsConstructor
public class LoseAllButOneLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseAllButOneLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int currentLife = gameData.getLife(controllerId);
        int lifeToLose = Math.max(0, currentLife - 1);

        lifeSupport.applyLifeLoss(gameData, controllerId, lifeToLose, entry.getCard().getName());

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            source.setLifeLostWhenEntered(Math.max(0, currentLife - gameData.getLife(controllerId)));
            source.setLifeLostWhenEnteredControllerId(controllerId);
        }
    }
}
