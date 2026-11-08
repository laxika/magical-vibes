package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToTriggeringPermanentEffect}: deals the effect's damage to the attacking
 * creature stored as the stack entry's non-targeting {@code targetId}. The attacker condition was
 * already checked when the trigger was declared, so the damage is dealt unconditionally here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DealDamageToTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final DamageSupport damageSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DealDamageToTriggeringPermanentEffect e = (DealDamageToTriggeringPermanentEffect) effect;

        Permanent triggering = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (triggering == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = evaluatedDamage;
        // Protection from the source and "prevent all damage this would deal" apply as to a targeted creature.
        if (damageSupport.isDamagePreventedForCreature(gameData, entry, triggering)) {
            return;
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" deals " + damage + " damage to ")
                .card(triggering.getCard()).text(".").build());
        damageSupport.dealCreatureDamage(gameData, entry, triggering, damage);
    }
}
