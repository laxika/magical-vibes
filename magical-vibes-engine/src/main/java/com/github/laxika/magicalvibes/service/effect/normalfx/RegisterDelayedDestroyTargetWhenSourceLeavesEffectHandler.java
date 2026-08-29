package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyTargetWhenSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDestroyTargetWhenSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDestroyTargetWhenSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || entry.getSourcePermanentId() == null) {
            log.info("Game {} - Delayed destroy-on-source-leave not registered (missing target or source)",
                    gameData.id);
            return;
        }

        gameData.queueDelayedAction(new DelayedDestroyTargetWhenSourceLeaves(
                entry.getSourcePermanentId(),
                target.getId(),
                entry.getControllerId(),
                entry.getCard()));
        log.info("Game {} - {} registers delayed trigger: if source leaves this turn, destroy {}",
                gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
