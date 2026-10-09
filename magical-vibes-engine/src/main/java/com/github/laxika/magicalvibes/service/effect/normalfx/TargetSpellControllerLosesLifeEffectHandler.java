package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetSpellControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetSpellControllerLosesLifeEffect) effect;
        UUID targetCardId = entry.getTriggeringCardId() != null
                ? entry.getTriggeringCardId() : entry.getTargetId();
        if (targetCardId == null && entry.getTriggeringPermanentControllerId() == null) return;

        for (StackEntry se : gameData.stack) {
            if (targetCardId != null && targetCardId.equals(se.getTargetableId())) {
                int amount = amountEvaluationService.evaluate(
                        gameData, e.amount(), AmountContext.forStackEntry(entry, null));
                lifeSupport.applyLifeLoss(gameData, se.getControllerId(), amount, entry.getCard().getName());
                return;
            }
        }
        UUID targetingSpellControllerId = entry.getTriggeringPermanentControllerId();
        if (targetingSpellControllerId == null) {
            targetingSpellControllerId = entry.getCounteredSpellControllerId();
        }
        if (targetingSpellControllerId != null) {
            int amount = amountEvaluationService.evaluate(
                    gameData, e.amount(), AmountContext.forStackEntry(entry, null));
            lifeSupport.applyLifeLoss(
                    gameData, targetingSpellControllerId, amount, entry.getCard().getName());
        } else {
            log.info("Game {} - Target spell no longer on stack for life loss", gameData.id);
        }
    }
}
