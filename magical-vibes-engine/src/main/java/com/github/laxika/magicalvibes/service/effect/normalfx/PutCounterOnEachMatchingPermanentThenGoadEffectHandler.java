package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentThenGoadEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves counter placement followed by goad on only the permanents that received counters. */
@Component
@RequiredArgsConstructor
public class PutCounterOnEachMatchingPermanentThenGoadEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnEachMatchingPermanentThenGoadEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterThenGoad = (PutCounterOnEachMatchingPermanentThenGoadEffect) effect;
        List<Permanent> candidates = new ArrayList<>();
        if (counterThenGoad.scope() == EachPermanentScope.TARGET_PLAYER) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getTargetId());
            if (battlefield != null) {
                candidates.addAll(battlefield);
            }
        } else {
            gameData.playerBattlefields.values().forEach(candidates::addAll);
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(source);

        List<UUID> goadedIds = new ArrayList<>();
        for (Permanent candidate : candidates) {
            if (!gameQueryService.isCreature(gameData, candidate)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    candidate, counterThenGoad.predicate(), context)) {
                continue;
            }
            int placed = permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, candidate, counterThenGoad.counterType(), counterThenGoad.count());
            if (placed > 0) {
                goadedIds.add(candidate.getId());
            }
        }

        for (UUID goadedId : goadedIds) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), new GoadTargetCreatureUntilNextTurnEffect(), goadedId,
                    null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        }
    }
}
