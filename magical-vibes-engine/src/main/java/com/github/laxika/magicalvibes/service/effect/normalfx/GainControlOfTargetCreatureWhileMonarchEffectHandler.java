package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureWhileMonarchEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Garland's control trigger against the player captured when the trigger was created. */
@Component
@RequiredArgsConstructor
public class GainControlOfTargetCreatureWhileMonarchEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetCreatureWhileMonarchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID monarchPlayerId = entry.getTriggeringPlayerId();
        if (monarchPlayerId == null || !monarchPlayerId.equals(gameData.monarchPlayerId)) {
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)
                    || !monarchPlayerId.equals(gameQueryService.findPermanentController(gameData, targetId))) {
                continue;
            }
            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target, effect,
                    EffectDuration.CONTINUOUS, null, entry.getCard().getName(), monarchPlayerId);
        }
    }
}
