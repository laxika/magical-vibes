package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
    private final GameBroadcastService gameBroadcastService;
    private final DamageSupport damageSupport;

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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" deals " + e.damage() + " damage to ").card(attacker.getCard()).text(".").build());
        damageSupport.dealCreatureDamage(gameData, entry, attacker, e.damage());
    }
}
