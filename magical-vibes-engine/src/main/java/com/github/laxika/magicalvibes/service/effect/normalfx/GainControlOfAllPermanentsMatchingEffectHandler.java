package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
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
 * {@link GainControlOfTargetEffect} floating effect. Optional rider effects are resolved in list
 * order for the snapshot of permanents gained by this resolution, after all control changes; a rider's
 * {@link SequenceEffect} steps run in order and a {@link ConditionalEffect} rider only when its condition holds
 * (Loki, God of Lies' "if it's your turn, untap that creature and it gains haste").
 */
@Component
@RequiredArgsConstructor
public class GainControlOfAllPermanentsMatchingEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GainControlOfTargetEffectHandler gainControlOfTargetEffectHandler;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfAllPermanentsMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainControlOfAllPermanentsMatchingEffect e = (GainControlOfAllPermanentsMatchingEffect) effect;
        UUID controllerId = entry.getControllerId();

        // Collect first: applyControlEffect moves permanents between battlefield lists, so we can't
        // seize while iterating. A new effect also applies to permanents already controlled.
        // The predicate may be source-relative (e.g. "permanents you own" — Gruul Charm), so the
        // resolving controller has to reach the evaluation as the filter context's source controller.
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId)
                .withSourceCardId(entry.getCard().getId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withTriggeringSpellTargetIds(triggeringSpellTargetIds(gameData, entry));

        List<Permanent> toSeize = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), filterContext)) {
                toSeize.add(permanent);
            }
        });

        ControlDuration duration = e.duration();
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(duration);
        List<Permanent> simultaneous = new ArrayList<>();
        for (Permanent permanent : toSeize) {
            if (duration.isSourceLinked()) {
                StackEntry individual = StackEntry.derivedFrom(entry, entry.getCard(), controllerId,
                        entry.getDescription(), List.of(controlEffect), permanent.getId(),
                        entry.getSourcePermanentId());
                individual.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
                individual.setNonTargeting(true);
                gainControlOfTargetEffectHandler.resolve(gameData, individual, controlEffect);
                continue;
            }
            simultaneous.add(permanent);
        }
        creatureControlService.applyControlEffectSimultaneously(gameData, controllerId, simultaneous,
                controlEffect, duration.toEffectDuration(), entry.getCard().getName());

        for (CardEffect thenEffect : riderSteps(gameData, entry, e.thenEffects())) {
            EffectHandler handler = effectHandlerRegistry.getHandler(thenEffect);
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

    /**
     * The rider effects to apply, in order: a {@link SequenceEffect}'s steps, and a {@link ConditionalEffect}'s
     * effect when its condition holds as the ability resolves ("if it's your turn, untap that creature").
     */
    private List<CardEffect> riderSteps(GameData gameData, StackEntry entry, List<CardEffect> riders) {
        List<CardEffect> steps = new ArrayList<>();
        for (CardEffect rider : riders) {
            switch (rider) {
                case SequenceEffect sequence -> steps.addAll(riderSteps(gameData, entry, sequence.steps()));
                case ConditionalEffect conditional -> {
                    if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forStackEntry(entry))) {
                        steps.addAll(riderSteps(gameData, entry, List.of(conditional.wrapped())));
                    }
                }
                default -> steps.add(rider);
            }
        }
        return steps;
    }

    /**
     * What the spell that triggered {@code entry} targeted as the ability triggered ("that creature", "those
     * permanents": the objects of the trigger event, which a target changed later doesn't move; compare Vesuvan
     * Duplimancy's ruling of 2022-09-09), its target and declared targets; the live spell's when no snapshot was
     * kept. Empty for an entry no spell triggered.
     */
    private static List<UUID> triggeringSpellTargetIds(GameData gameData, StackEntry entry) {
        if (entry.getTriggeringCardId() == null) {
            return List.of();
        }
        StackEntry spell = entry.getTriggeringSpellSnapshot() != null ? entry.getTriggeringSpellSnapshot()
                : gameData.stack.stream()
                        .filter(candidate -> candidate != entry
                                && entry.getTriggeringCardId().equals(candidate.getTargetableId()))
                        .findFirst()
                        .orElse(null);
        if (spell == null) {
            return List.of();
        }
        List<UUID> targetIds = new ArrayList<>();
        if (spell.getTargetId() != null) {
            targetIds.add(spell.getTargetId());
        }
        targetIds.addAll(spell.getDeclaredTargetIds());
        return targetIds;
    }
}
