package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainLifeEqualToTargetCreaturePowerEffect}: the controller gains life equal to the
 * target creature's effective power. Mirrors {@code GainLifeEffectHandler}'s TargetPower path — no
 * legal target at resolution evaluates to 0 (fizzle-safe).
 */
@Component
@RequiredArgsConstructor
public class GainLifeEqualToTargetCreaturePowerEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeEqualToTargetCreaturePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int amount = amountEvaluationService.evaluate(gameData, new TargetPower(),
                AmountContext.forStackEntry(entry, null));
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), amount, null,
                entry.getCard(), entry.getEntryType());
    }
}
