package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfAllPermanentsTargetPlayerControlsEffect} (Gilt-Leaf Archdruid,
 * Hellkite Tyrant). Gains the controller permanent control of every permanent matching the effect's
 * filter that the target player controls at resolution, reusing the layer-2 control machinery with a
 * per-permanent {@link GainControlOfTargetEffect} floating effect.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfAllPermanentsTargetPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect CONTROL_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.PERMANENT);

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfAllPermanentsTargetPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainControlOfAllPermanentsTargetPlayerControlsEffect seizeEffect =
                (GainControlOfAllPermanentsTargetPlayerControlsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;
        if (targetPlayerId.equals(entry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (seizeEffect.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, seizeEffect.filter())) {
                continue;
            }
            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), permanent,
                    CONTROL_EFFECT, ControlDuration.PERMANENT.toEffectDuration(), null,
                    entry.getCard().getName());
        }
    }
}
