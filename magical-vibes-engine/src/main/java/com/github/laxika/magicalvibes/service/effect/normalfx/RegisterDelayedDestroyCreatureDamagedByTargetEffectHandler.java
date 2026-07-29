package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDamagedByWatchedCreature;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyCreatureDamagedByTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDestroyCreatureDamagedByTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDestroyCreatureDamagedByTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Delayed destroy-on-combat-damage not registered (target gone)", gameData.id);
            return;
        }

        gameData.queueDelayedAction(new DelayedDestroyCreatureDamagedByWatchedCreature(
                target.getId(),
                entry.getControllerId(),
                entry.getCard()));
        log.info("Game {} - {} registers delayed trigger: creatures damaged in combat by {} are destroyed",
                gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
