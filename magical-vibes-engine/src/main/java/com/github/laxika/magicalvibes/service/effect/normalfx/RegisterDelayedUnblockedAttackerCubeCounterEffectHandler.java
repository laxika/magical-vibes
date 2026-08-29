package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerCubeCounter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerCubeCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Registers a source-owned unblocked-attack trigger for one target creature until end of turn. */
@Component
@RequiredArgsConstructor
public class RegisterDelayedUnblockedAttackerCubeCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedUnblockedAttackerCubeCounterEffect.class;
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

        gameData.queueDelayedAction(new DelayedUnblockedAttackerCubeCounter(
                targetId, entry.getControllerId(), entry.getCard()));
    }
}
