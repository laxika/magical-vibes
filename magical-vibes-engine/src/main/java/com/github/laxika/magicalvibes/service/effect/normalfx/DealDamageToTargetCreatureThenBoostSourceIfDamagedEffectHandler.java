package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureThenBoostSourceIfDamagedEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BoostSelfEffectHandler boostSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        boolean targetMatches = predicateEvaluationService.matchesPermanentPredicate(
                gameData, target, damageEffect.boostTargetPredicate());
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);

        if (damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
            return;
        }
        int actualDamage = damageSupport.dealCreatureDamage(gameData, entry, target, damage);
        if (actualDamage > 0 && targetMatches) {
            boostSelfEffectHandler.resolve(gameData, entry,
                    new BoostSelfEffect(damageEffect.powerBoost(), damageEffect.toughnessBoost()));
        }
    }
}
