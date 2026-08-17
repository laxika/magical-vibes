package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final RemoveKeywordEffectHandler removeKeywordEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameData.playerIds.contains(targetId)
                ? null : gameQueryService.findPermanentById(gameData, targetId);
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);

        if (target != null && gameQueryService.isCreature(gameData, target)) {
            boolean damagePrevented = damageSupport.isDamageSourcePreventedWithLog(gameData, entry)
                    || (gameQueryService.isDamagePreventable(gameData)
                    && damageSupport.isSourcePermanentPreventedFromDealingDamage(gameData, entry))
                    || damageSupport.isDamagePreventedForCreature(gameData, entry, target);
            if (!damagePrevented) {
                int actualDamage = damageSupport.dealCreatureDamage(gameData, entry, target, damage);
                if (actualDamage > 0) {
                    if (damageEffect.exileInsteadOfDie()) {
                        target.setExileInsteadOfDieThisTurn(true);
                    }
                    removeKeywordEffectHandler.resolve(gameData, entry,
                            new RemoveKeywordEffect(damageEffect.keyword(), GrantScope.TARGET,
                                    EffectDuration.UNTIL_END_OF_TURN));
                }
            }
        } else {
            damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
