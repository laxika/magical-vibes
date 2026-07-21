package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfAllPermanentsMatchingEffect} (Karrthus, Tyrant of Jund). Gains the
 * controller permanent control of every permanent matching the effect's predicate that they do not
 * already control, reusing the layer-2 control machinery with a per-permanent
 * {@link GainControlOfTargetEffect} floating effect.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfAllPermanentsMatchingEffectHandler implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect CONTROL_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.PERMANENT);

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfAllPermanentsMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainControlOfAllPermanentsMatchingEffect e = (GainControlOfAllPermanentsMatchingEffect) effect;
        UUID controllerId = entry.getControllerId();

        // Collect first: applyControlEffect moves permanents between battlefield lists, so we can't
        // seize while iterating. Skip permanents the controller already controls (a no-op steal).
        List<Permanent> toSeize = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (playerId.equals(controllerId)) return;
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.predicate())) {
                toSeize.add(permanent);
            }
        });

        for (Permanent permanent : toSeize) {
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    CONTROL_EFFECT, ControlDuration.PERMANENT.toEffectDuration(), null,
                    entry.getCard().getName());
        }
    }
}
