package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BoostSelfEffectHandler boostSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect) effect;
        // "It" is the creature that entered, as it is when the ability resolves: nothing if it has left (CR 608.2h).
        Permanent target = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
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
        int damage = evaluatedDamage;

        if (damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
            return;
        }
        int actualDamage = damageSupport.dealCreatureDamage(gameData, entry, target, damage);
        // Damage a redirect effect moved onto another permanent is still this ability's damage (CR 614.9),
        // so the permanent that actually took it is the one checked against the predicate.
        boolean matchingPermanentDamaged = entry.getPermanentsDealtDamageThisResolution().stream()
                .map(damagedId -> gameQueryService.findPermanentById(gameData, damagedId))
                .anyMatch(damaged -> damaged != null && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, damaged, damageEffect.boostTargetPredicate()));
        if (actualDamage > 0 && targetMatches || matchingPermanentDamaged) {
            boostSelfEffectHandler.resolve(gameData, entry,
                    new BoostSelfEffect(damageEffect.powerBoost(), damageEffect.toughnessBoost()));
        }
    }
}
