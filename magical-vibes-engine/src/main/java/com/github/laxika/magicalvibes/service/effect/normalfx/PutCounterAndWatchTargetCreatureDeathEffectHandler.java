package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterAndWatchTargetCreatureDeathEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves counter placement and registers the linked exact-permanent death trigger. */
@Component
@RequiredArgsConstructor
public class PutCounterAndWatchTargetCreatureDeathEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterAndWatchTargetCreatureDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterAndWatchTargetCreatureDeathEffect watched =
                (PutCounterAndWatchTargetCreatureDeathEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !predicateEvaluationService.matchesPermanentPredicate(
                    target, watched.targetRestriction(), FilterContext.of(gameData)
                            .withSourceControllerId(entry.getControllerId())
                            .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                            .withSourcePermanentId(entry.getSourcePermanentId()))) {
                continue;
            }

            int placed = permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, watched.counterType(), 1);
            if (placed > 0) {
                gameData.targetedCreatureDeathTriggerWatchers.add(
                        new TargetedCreatureDeathTriggerWatcher(
                                target.getId(), watched.counterType(), entry.getControllerId(),
                                entry.getCard(), watched.deathEffect()));
            }
        }
    }
}
