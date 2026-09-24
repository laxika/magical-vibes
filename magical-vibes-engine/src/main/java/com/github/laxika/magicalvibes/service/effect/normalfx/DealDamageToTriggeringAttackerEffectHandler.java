package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToTriggeringAttackerEffect}: deals the effect's damage to the attacking
 * creature stored as the stack entry's non-targeting {@code targetId}. The attacker condition was
 * already checked when the trigger was declared, so the damage is dealt unconditionally here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DealDamageToTriggeringAttackerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final DamageSupport damageSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTriggeringAttackerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DealDamageToTriggeringAttackerEffect e = (DealDamageToTriggeringAttackerEffect) effect;

        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (attacker == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" deals " + damage + " damage to ").card(attacker.getCard()).text(".").build());
        damageSupport.dealCreatureDamage(gameData, entry, attacker, damage);
    }
}
