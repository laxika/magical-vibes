package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetCreatureStatEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainLifeEqualToTargetCreatureStatEffect}: the controller gains life equal to the
 * effect's amount evaluated against the target creature. Mirrors {@code GainLifeEffectHandler}'s
 * TargetPower path — no legal target at resolution evaluates to 0 (fizzle-safe).
 */
@Component
@RequiredArgsConstructor
public class GainLifeEqualToTargetCreatureStatEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeEqualToTargetCreatureStatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainLifeEqualToTargetCreatureStatEffect statEffect =
                (GainLifeEqualToTargetCreatureStatEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, statEffect.amount(),
                AmountContext.forStackEntry(entry, null));
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), amount, null,
                entry.getCard(), entry.getEntryType());
    }
}
