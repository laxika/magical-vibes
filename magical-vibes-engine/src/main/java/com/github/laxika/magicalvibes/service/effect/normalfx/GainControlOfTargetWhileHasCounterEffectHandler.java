package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves counter-conditioned control and lets the control service expire it when needed. */
@Component
@RequiredArgsConstructor
public class GainControlOfTargetWhileHasCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetWhileHasCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterConditioned = (GainControlOfTargetWhileHasCounterEffect) effect;
        for (UUID targetId : targetIds(entry, effect)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || target.getCounterCount(counterConditioned.counterType()) <= 0) {
                continue;
            }
            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                    counterConditioned, EffectDuration.PERMANENT, null, entry.getCard().getName());
        }
    }

    private static List<UUID> targetIds(StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        if (!targets.isEmpty()) {
            return targets;
        }
        return entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
    }
}
