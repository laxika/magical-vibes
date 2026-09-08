package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link EachPlayerGainsControlOfOwnedCreaturesEffect} by returning every owned creature
 * to its owner with the normal layer-2 control machinery.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerGainsControlOfOwnedCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerGainsControlOfOwnedCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<OwnedCreature> creaturesToReturn = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                return;
            }
            UUID ownerId = gameData.defaultControllerOf(permanent.getId());
            if (ownerId != null && !ownerId.equals(controllerId)) {
                creaturesToReturn.add(new OwnedCreature(ownerId, permanent));
            }
        });

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (OwnedCreature ownedCreature : creaturesToReturn) {
            creatureControlService.applyControlEffect(gameData, ownedCreature.ownerId(), ownedCreature.permanent(),
                    controlEffect, EffectDuration.PERMANENT, null, entry.getCard().getName());
        }
    }

    private record OwnedCreature(UUID ownerId, Permanent permanent) {
    }
}
