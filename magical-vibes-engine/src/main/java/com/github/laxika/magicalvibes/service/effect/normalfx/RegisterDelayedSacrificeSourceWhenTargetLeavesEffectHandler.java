package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSacrificeSourceWhenTargetLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSacrificeSourceWhenTargetLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || entry.getSourcePermanentId() == null) {
            log.info("Game {} - Delayed sacrifice-on-leave not registered (missing target or source)",
                    gameData.id);
            return;
        }

        gameData.queueDelayedAction(new DelayedSacrificeSourceWhenTargetLeaves(
                target.getId(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                entry.getCard()));
        log.info("Game {} - {} registers delayed trigger: if {} leaves this turn, sacrifice source",
                gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
