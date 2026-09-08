package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerGainLife;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerGainLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Registers a controller-owned unblocked-attack trigger for one target creature until end of turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedUnblockedAttackerGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedUnblockedAttackerGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        UUID targetId = targetIds.getFirst();
        if (gameQueryService.findPermanentById(gameData, targetId) == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedUnblockedAttackerGainLife(
                targetId, entry.getControllerId(), entry.getCard()));
        log.info("Game {} - {} watches a creature for an unblocked attack until end of turn",
                gameData.id, entry.getCard().getName());
    }
}
