package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Destroys every target of the entry still on the battlefield and matching the effect's filter as one event (a single
 * spell or ability destroys its targets simultaneously, so they see each other die), and records the step on the
 * entry's resolution scratchpad: the departure of every permanent it was told to destroy, those that stayed included,
 * so {@code StepKind.DESTROY}'s outcome is what was destroyed (wherever a replacement put it) with the controllers as
 * they were destroyed (CR 608.2h). It records even when nothing was destroyed.
 */
@Component
@RequiredArgsConstructor
public class DestroyEachTargetPermanentEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEachTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyEachTargetPermanentEffect) effect;
        entry.recordDestroyDepartures(List.of());

        List<UUID> targets = entry.getTargetIds();
        if (targets.isEmpty() && entry.getTargetId() != null) {
            targets = List.of(entry.getTargetId());
        }

        List<Permanent> toDestroy = new ArrayList<>();
        HashSet<UUID> seenTargetIds = new HashSet<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        for (UUID targetId : targets) {
            if (!seenTargetIds.add(targetId)) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            if (destroy.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(target, destroy.filter(), filterContext)) {
                continue;
            }
            toDestroy.add(target);
        }

        DestructionSupport.BatchOutcome outcome = destructionSupport.destroyBatchWithOutcome(
                gameData, toDestroy, entry.getCard().getName(), destroy.cannotBeRegenerated());
        entry.recordDestroyDepartures(outcome.results());
    }
}
