package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenBoostIfCreatureDamagedEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetThenBoostIfCreatureDamagedEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final BoostTargetCreatureEffectHandler boostTargetCreatureEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetThenBoostIfCreatureDamagedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToAnyTargetThenBoostIfCreatureDamagedEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
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

        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);

        for (Map.Entry<UUID, Integer> dealtDamage : gameData.damageDealtToPermanentsThisTurn.entrySet()) {
            int previous = damageBefore.getOrDefault(dealtDamage.getKey(), 0);
            if (dealtDamage.getValue() <= previous) {
                continue;
            }
            Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, dealtDamage.getKey());
            if (damagedPermanent != null && gameQueryService.isCreature(gameData, damagedPermanent)) {
                boostTargetCreatureEffectHandler.resolveForTarget(gameData, entry, damagedPermanent,
                        new BoostTargetCreatureEffect(
                                damageEffect.powerBoost(), damageEffect.toughnessBoost()));
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
