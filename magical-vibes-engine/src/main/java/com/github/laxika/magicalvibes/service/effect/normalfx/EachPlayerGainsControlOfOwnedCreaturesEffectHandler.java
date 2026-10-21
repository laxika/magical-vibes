package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
            if (ownerId != null && needsPermanentControlByOwner(gameData, permanent, ownerId, controllerId)) {
                creaturesToReturn.add(new OwnedCreature(ownerId, permanent));
            }
        });

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (OwnedCreature ownedCreature : creaturesToReturn) {
            creatureControlService.applyControlEffect(gameData, ownedCreature.ownerId(), ownedCreature.permanent(),
                    controlEffect, EffectDuration.PERMANENT, null, entry.getCard().getName());
        }
    }

    /**
     * An owner already holding a creature through a temporary control effect (a stolen creature that
     * was regained until end of turn) still gains control of it permanently, so the permanent effect
     * outlives the temporary one. A creature whose newest control effect already gives it to its owner
     * permanently needs no new effect.
     */
    private boolean needsPermanentControlByOwner(GameData gameData, Permanent permanent, UUID ownerId,
                                                 UUID controllerId) {
        if (!ownerId.equals(controllerId)) {
            return true;
        }
        FloatingContinuousEffect newest = gameData.newestControlEffectFor(permanent.getId());
        return newest != null && !(newest.duration() == EffectDuration.PERMANENT
                && ownerId.equals(gameData.resolveControlEffectController(newest)));
    }

    private record OwnedCreature(UUID ownerId, Permanent permanent) {
    }
}
