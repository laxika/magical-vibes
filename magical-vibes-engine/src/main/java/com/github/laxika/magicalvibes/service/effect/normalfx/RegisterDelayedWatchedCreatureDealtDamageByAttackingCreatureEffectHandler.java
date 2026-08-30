package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamageByAttackingCreature;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Registers a controller-owned trigger for damage dealt to one Wall by attacking creatures. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedWatchedCreatureDealtDamageByAttackingCreatureEffect) effect;
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

        gameData.queueDelayedAction(new DelayedWatchedCreatureDealtDamageByAttackingCreature(
                targetId, entry.getControllerId(), e.effects(), entry.getCard()));
        log.info("Game {} - {} watches a Wall for damage from attacking creatures until end of turn",
                gameData.id, entry.getCard().getName());
    }
}
