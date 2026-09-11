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
        List<UUID> targetPlayerIds = e.targetGroup() >= 0
                ? entry.targetsForGroup(e.targetGroup()) : entry.targetsForEffect(effect);
        if (e.targetGroup() < 0 && targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = Collections.singletonList(entry.getTargetId());
        }
        if (targetPlayerIds.isEmpty()) {
            return;
        }
        AmountContext amountContext = AmountContext.forStackEntry(entry, null);
        for (UUID targetPlayerId : targetPlayerIds) {
            if (gameData.playerIds.contains(targetPlayerId)) {
                int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                        amountContext.withControllerId(targetPlayerId)
                                .withTargetPermanentId(targetPlayerId));
                lifeSupport.applyGainLife(gameData, targetPlayerId, amount);
            }
        }
    }
}
