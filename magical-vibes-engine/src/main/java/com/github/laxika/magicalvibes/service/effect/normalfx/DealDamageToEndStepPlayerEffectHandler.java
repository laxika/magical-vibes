package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEndStepPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEndStepPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEndStepPlayerEffect) effect;
        UUID targetId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetId)) {
            return;
        }

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null) {
                source = entry.getSourcePermanentSnapshot();
            }
            int amount = amountEvaluationService.evaluate(
                    gameData, e.amount(), AmountContext.forStackEntry(entry, source));
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
