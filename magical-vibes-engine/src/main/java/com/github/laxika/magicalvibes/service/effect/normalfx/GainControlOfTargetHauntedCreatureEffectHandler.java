package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Gives Kaya's controller control of the targeted haunted creature indefinitely. */
@Component
@RequiredArgsConstructor
public class GainControlOfTargetHauntedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetHauntedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = targetId(entry, effect);
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        creatureControlService.applyControlEffect(
                gameData,
                entry.getControllerId(),
                target,
                new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS),
                EffectDuration.WHILE_SOURCE_REMAINS,
                target.getId(),
                entry.getCard().getName());
    }

    private UUID targetId(StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        return targets.isEmpty() ? entry.getTargetId() : targets.getFirst();
    }
}
