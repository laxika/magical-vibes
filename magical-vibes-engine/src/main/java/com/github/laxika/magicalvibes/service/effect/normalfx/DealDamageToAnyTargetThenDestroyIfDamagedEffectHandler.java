package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenDestroyIfDamagedEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetThenDestroyIfDamagedEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetThenDestroyIfDamagedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToAnyTargetThenDestroyIfDamagedEffect) effect;
        if (entry.getTargetId() == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = evaluatedDamage;

        Map<UUID, Integer> damageBefore = new HashMap<>(gameData.damageDealtToPermanentsThisTurn);
        damageSupport.resolveAnyTargetDamage(gameData, entry, entry.getTargetId(), damage, false);

        List<UUID> damagedPermanentIds = gameData.damageDealtToPermanentsThisTurn.entrySet().stream()
                .filter(dealtDamage -> dealtDamage.getValue()
                        > damageBefore.getOrDefault(dealtDamage.getKey(), 0))
                .map(Map.Entry::getKey)
                .toList();
        for (UUID damagedPermanentId : damagedPermanentIds) {
            Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, damagedPermanentId);
            if (damagedPermanent != null && predicateEvaluationService.matchesPermanentPredicate(
                    gameData, damagedPermanent, damageEffect.destroyTargetPredicate())) {
                destructionSupport.tryDestroyAndLog(gameData, damagedPermanent, entry.getCard().getName(), false);
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
