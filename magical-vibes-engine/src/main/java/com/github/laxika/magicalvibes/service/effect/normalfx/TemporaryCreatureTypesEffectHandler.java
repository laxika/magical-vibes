package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a temporary grant of creature types without granting a keyword ability. */
@Component
public class TemporaryCreatureTypesEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAllCreatureTypesToOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        if (targets.isEmpty() && entry.getTargetId() != null) {
            targets = List.of(entry.getTargetId());
        }
        for (UUID targetId : targets) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), effect,
                    targetId, null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
