package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureThenDestroyIfDamagedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureThenDestroyIfDamagedEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureThenDestroyIfDamagedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTargetCreatureThenDestroyIfDamagedEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        boolean targetMatches = predicateEvaluationService.matchesPermanentPredicate(
                gameData, target, damageEffect.destroyTargetPredicate());
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);

        if (damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
            return;
        }
        int actualDamage = damageSupport.dealCreatureDamage(gameData, entry, target, damage);
        if (actualDamage > 0 && targetMatches) {
            destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);
        }
    }
}
