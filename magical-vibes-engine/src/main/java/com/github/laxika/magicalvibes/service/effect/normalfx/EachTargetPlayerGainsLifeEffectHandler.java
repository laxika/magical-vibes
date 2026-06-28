package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerGainsLifeEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachTargetPlayerGainsLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetPlayerGainsLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachTargetPlayerGainsLifeEffect) effect;
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.isEmpty()) {
            return;
        }
        for (UUID targetPlayerId : targets) {
            if (!gameData.playerIds.contains(targetPlayerId)) {
                continue;
            }
            lifeSupport.applyGainLife(gameData, targetPlayerId, e.amount());
        }
    }
}
