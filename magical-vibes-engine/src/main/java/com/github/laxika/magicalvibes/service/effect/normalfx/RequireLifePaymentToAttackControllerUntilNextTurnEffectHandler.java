package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GlobalAttackLifeTaxEffect;
import com.github.laxika.magicalvibes.model.effect.RequireLifePaymentToAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequireLifePaymentToAttackControllerUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RequireLifePaymentToAttackControllerUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RequireLifePaymentToAttackControllerUntilNextTurnEffect tax =
                (RequireLifePaymentToAttackControllerUntilNextTurnEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, tax.lifeCostPerAttacker(),
                AmountContext.forStackEntry(entry, source));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                new GlobalAttackLifeTaxEffect(amount),
                null,
                entry.getControllerId(),
                null,
                EffectDuration.UNTIL_YOUR_NEXT_TURN,
                0));
    }
}
