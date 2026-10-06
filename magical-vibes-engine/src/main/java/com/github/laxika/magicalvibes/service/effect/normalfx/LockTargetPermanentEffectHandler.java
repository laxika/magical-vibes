package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LockTargetPermanentEffect} by stamping a {@link FloatingContinuousEffect} onto
 * the target creature. The floating effect carries the lock facts (read via
 * {@code PermanentLockEffect} by the combat and ability-activation services) and expires through
 * the standard duration machinery ({@code UNTIL_END_OF_TURN} at cleanup, {@code UNTIL_YOUR_NEXT_TURN}
 * at the ability controller's next turn start).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LockTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LockTargetPermanentEffect lock = (LockTargetPermanentEffect) effect;
        if (entry.getDeclaredTargetIds().isEmpty()) {
            lockOne(gameData, entry, lock, entry.getTargetId());
            return;
        }
        for (UUID targetId : entry.targetsForEffect(lock)) {
            lockOne(gameData, entry, lock, targetId);
        }
    }

    private void lockOne(GameData gameData, StackEntry entry, LockTargetPermanentEffect lock, UUID targetId) {
        if (targetId == null) {
            return;
        }
        if (lock.duration() == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD
                || lock.duration() == EffectDuration.WHILE_SOURCE_REMAINS
                || lock.duration() == EffectDuration.WHILE_SOURCE_TAPPED
                || lock.duration() == EffectDuration.WHILE_SOURCE_REMAINS_TAPPED
                || lock.duration() == EffectDuration.WHILE_ATTACHED) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                return;
            }
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            log.info("Game {} - lock ability fizzles, target left the battlefield", gameData.id);
            return;
        }
        TargetPredicate declaredTarget = lock.declaredTarget() != null
                ? lock.declaredTarget() : TargetPredicates.creature();
        FilterContext targetContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        if (!targetPredicateEvaluationService.matchesPermanent(declaredTarget, target, targetContext)) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), lock, target.getId(), null, null, lock.duration(), 0));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " " + describe(lock) + "."));
        log.info("Game {} - {} locked ({})", gameData.id, target.getCard().getName(), describe(lock));
    }

    private String describe(LockTargetPermanentEffect lock) {
        String combat;
        if (lock.locksAttacking() && lock.locksBlocking()) {
            combat = "can't attack or block";
        } else if (lock.locksBlocking()) {
            combat = "can't block";
        } else if (lock.locksAttacking()) {
            combat = "can't attack";
        } else {
            combat = null;
        }
        if (!lock.locksActivatedAbilities()) {
            return combat == null ? "is locked" : combat;
        }
        return combat == null
                ? "can't have its activated abilities activated"
                : combat + " and its activated abilities can't be activated";
    }
}
