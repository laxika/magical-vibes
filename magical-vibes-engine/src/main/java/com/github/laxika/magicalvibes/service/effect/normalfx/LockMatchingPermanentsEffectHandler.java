package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LockMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
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
}
