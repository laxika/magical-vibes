package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
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

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GainControlOfTargetEffectHandler gainControlOfTargetEffectHandler;

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

        List<Permanent> toSeize = new ArrayList<>();
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (seizeEffect.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, seizeEffect.filter())) {
                continue;
            }
            toSeize.add(permanent);
        }

        ControlDuration duration = seizeEffect.duration();
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(duration);
        for (Permanent permanent : toSeize) {
            if (duration.isSourceLinked()) {
                StackEntry individual = new StackEntry(entry.getEntryType(), entry.getCard(),
                        entry.getControllerId(), entry.getDescription(), List.of(controlEffect),
                        permanent.getId(), entry.getSourcePermanentId());
                individual.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
                individual.setNonTargeting(true);
                gainControlOfTargetEffectHandler.resolve(gameData, individual, controlEffect);
                continue;
            }
            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), permanent,
                    controlEffect, duration.toEffectDuration(), null,
                    entry.getCard().getName());
        }

        for (CardEffect thenEffect : seizeEffect.thenEffects()) {
            var handler = effectHandlerRegistry.getHandler(thenEffect);
            if (handler == null) {
                continue;
            }
            for (Permanent permanent : toSeize) {
                UUID previousTargetId = entry.getTargetId();
                entry.setTargetId(permanent.getId());
                try {
                    handler.resolve(gameData, entry, thenEffect);
                } finally {
                    entry.setTargetId(previousTargetId);
                }
            }
        }
    }
}
