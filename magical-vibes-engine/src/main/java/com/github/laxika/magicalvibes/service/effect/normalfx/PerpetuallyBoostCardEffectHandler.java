package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostCardEffect boost = (PerpetuallyBoostCardEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);
        PerpetualCardPowerToughnessSupport.remember(
                gameData, boost.card(), powerBoost, toughnessBoost);

        Permanent target = source;
        if (entry.getTriggeringPermanentId() != null) {
            target = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        }
        if (target != null && target.getCard().getId().equals(boost.card().getId())) {
            PerpetualCardPowerToughnessSupport.applyToPermanent(
                    gameData, entry.getControllerId(), target,
                    powerBoost, toughnessBoost);
        }
    }
}
