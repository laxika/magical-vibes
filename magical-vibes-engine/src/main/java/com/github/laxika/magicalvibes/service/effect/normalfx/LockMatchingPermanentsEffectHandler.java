package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LockMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LockMatchingPermanentsEffect} by stamping one floating
 * {@link LockTargetPermanentEffect} onto every permanent matching the predicate, reusing the
 * single-target lock's readers ({@code CombatAttackService}, {@code GameQueryService},
 * {@code AbilityActivationService}) and duration expiry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockMatchingPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LockMatchingPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LockMatchingPermanentsEffect e = (LockMatchingPermanentsEffect) effect;

        LockTargetPermanentEffect lock = new LockTargetPermanentEffect(
                e.locksAttacking(), e.locksBlocking(), e.locksActivatedAbilities(), e.duration());

        if (e.affectsLaterPermanents()) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), lock, null, null, resolveManaValueX(e.predicate(), entry.getXValue()),
                    lock.duration(), 0));
            gameLogService.append(gameData, GameLog.text("Matching permanents can't attack or block this turn."));
            return;
        }

        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withXValue(entry.getXValue());

        List<Permanent> candidates = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            candidates.addAll(battlefield);
        }

        for (Permanent permanent : candidates) {
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), ctx)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), lock, permanent.getId(), null, null, lock.duration(), 0));
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is detained."));
            log.info("Game {} - {} locked by {}", gameData.id, permanent.getCard().getName(),
                    entry.getCard().getName());
        }
    }

    private PermanentPredicate resolveManaValueX(PermanentPredicate predicate, int xValue) {
        if (predicate instanceof PermanentManaValueEqualsXPredicate) {
            return new PermanentAllOfPredicate(List.of(
                    new PermanentMinManaValuePredicate(xValue), new PermanentMaxManaValuePredicate(xValue)));
        }
        if (predicate instanceof PermanentAllOfPredicate all) {
            return new PermanentAllOfPredicate(all.predicates().stream()
                    .map(part -> resolveManaValueX(part, xValue)).toList());
        }
        return predicate;
    }
}
