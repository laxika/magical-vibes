package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamage;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealtDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Registers a controller-owned damage trigger for one target creature until end of turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedWatchedCreatureDealtDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedWatchedCreatureDealtDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedWatchedCreatureDealtDamageEffect) effect;
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

        gameData.queueDelayedAction(new DelayedWatchedCreatureDealtDamage(
                targetId, entry.getControllerId(), e.effects(), entry.getCard()));
        log.info("Game {} - {} watches a creature for damage dealt to it until end of turn",
                gameData.id, entry.getCard().getName());
    }
}
