package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureWithExcessToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureWithExcessToControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final TransformSelfEffectHandler transformSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureWithExcessToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureWithExcessToControllerEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        int evaluatedDamage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, gameQueryService.findPermanentById(
                        gameData, entry.getSourcePermanentId())));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);
        int markedDamageBefore = target.getMarkedDamage();
        int excessDamage = damageSupport.computeExcessDamageToCreature(
                gameData, target, damage, markedDamageBefore, false);
        int damageToCreature = Math.max(0, damage - excessDamage);

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        damageSupport.dealCreatureDamage(gameData, entry, target, damageToCreature);
        if (excessDamage <= 0 || targetControllerId == null) {
            return;
        }

        damageSupport.dealDamageToPlayer(gameData, entry, targetControllerId, excessDamage);
        if (e.transformSourceIfExcess()) {
            transformSelfEffectHandler.resolve(gameData, entry, new TransformSelfEffect());
        }
    }
}
