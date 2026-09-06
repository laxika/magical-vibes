package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerGainsLifeEffect) effect;
        List<UUID> targetPlayerIds = entry.targetsForEffect(effect);
        if (targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = Collections.singletonList(entry.getTargetId());
        }
        if (targetPlayerIds.isEmpty()) {
            return;
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, null));
        for (UUID targetPlayerId : targetPlayerIds) {
            if (gameData.playerIds.contains(targetPlayerId)) {
                lifeSupport.applyGainLife(gameData, targetPlayerId, amount);
            }
        }
    }
}
