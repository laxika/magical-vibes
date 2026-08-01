package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDealingCombatDamageToPlaneswalker;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyCreatureDealingCombatDamageToPlaneswalkerEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Registers the source planeswalker for Vraska the Unseen's delayed combat-damage trigger.
 */
@Slf4j
@Component
public class RegisterDelayedDestroyCreatureDealingCombatDamageToPlaneswalkerEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDestroyCreatureDealingCombatDamageToPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID planeswalkerId = entry.getSourcePermanentId();
        if (planeswalkerId == null) {
            log.info("Game {} - delayed planeswalker combat-damage trigger was not registered without a source permanent",
                    gameData.id);
            return;
        }

        gameData.queueDelayedAction(new DelayedDestroyCreatureDealingCombatDamageToPlaneswalker(
                planeswalkerId, entry.getControllerId(), entry.getCard()));
    }
}
