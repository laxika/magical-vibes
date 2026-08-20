package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Returns matching permanents to their respective owners with permanent control. */
@Component
@RequiredArgsConstructor
public class EachPlayerGainsControlOfOwnedPermanentsMatchingEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerGainsControlOfOwnedPermanentsMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var resolvingEffect = (EachPlayerGainsControlOfOwnedPermanentsMatchingEffect) effect;
        List<OwnedPermanent> toReturn = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            UUID ownerId = gameData.defaultControllerOf(permanent.getId());
            if (ownerId != null && !ownerId.equals(controllerId)
                    && predicateEvaluationService.matchesPermanentPredicate(
                    permanent, resolvingEffect.filter(), FilterContext.of(gameData).withSourceControllerId(ownerId))) {
                toReturn.add(new OwnedPermanent(ownerId, permanent));
            }
        });

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (OwnedPermanent ownedPermanent : toReturn) {
            creatureControlService.applyControlEffect(gameData, ownedPermanent.ownerId(), ownedPermanent.permanent(),
                    controlEffect, EffectDuration.PERMANENT, null, entry.getCard().getName());
        }
    }

    private record OwnedPermanent(UUID ownerId, Permanent permanent) {
    }
}
