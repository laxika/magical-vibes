package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeTargetWhenSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSacrificeTargetWhenSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSacrificeTargetWhenSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || entry.getSourcePermanentId() == null) {
            log.info("Game {} - Delayed sacrifice-target-on-source-leave not registered (missing target or source)",
                    gameData.id);
            return;
        }

        gameData.queueDelayedAction(new DelayedSacrificeTargetWhenSourceLeaves(
                entry.getSourcePermanentId(),
                target.getId(),
                entry.getControllerId(),
                entry.getCard()));
        log.info("Game {} - {} registers delayed trigger: if source leaves this turn, sacrifice {}",
                gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
