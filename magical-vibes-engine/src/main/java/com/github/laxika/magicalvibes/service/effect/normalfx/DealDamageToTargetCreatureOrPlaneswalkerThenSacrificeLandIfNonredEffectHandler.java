package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffectHandler
        implements NormalEffectHandlerBean {

    private static final PermanentPredicate NONRED = new PermanentNotPredicate(
            new PermanentColorInPredicate(Set.of(CardColor.RED)));

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Map<UUID, Integer> damageBefore = new HashMap<>(gameData.damageDealtToPermanentsThisTurn);
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);

        if (!damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
            damageSupport.dealCreatureDamage(gameData, entry, target, damage);
        }
        boolean nonredPermanentDamaged = gameData.damageDealtToPermanentsThisTurn.entrySet().stream()
                .anyMatch(dealtDamage -> {
                    int previous = damageBefore.getOrDefault(dealtDamage.getKey(), 0);
                    Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, dealtDamage.getKey());
                    return dealtDamage.getValue() > previous
                            && damagedPermanent != null
                            && predicateEvaluationService.matchesPermanentPredicate(gameData, damagedPermanent, NONRED);
                });
        if (nonredPermanentDamaged) {
            sacrificePermanentsEffectHandler.resolve(gameData, entry,
                    new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
        }
    }
}
