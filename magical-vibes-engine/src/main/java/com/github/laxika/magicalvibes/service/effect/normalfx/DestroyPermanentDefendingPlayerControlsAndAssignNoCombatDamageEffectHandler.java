package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect}
 * after its permanent target and the wrapped payment choice have both been made: destroys the target as the
 * resolving step, then the source assigns no combat damage.
 */
@Component
@RequiredArgsConstructor
public class DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect) effect;
        UUID targetId = entry.getTargetId();
        UUID defenderId = entry.getAttackedTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (targetId == null || defenderId == null || sourcePermanentId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID targetController = gameQueryService.findPermanentController(gameData, targetId);
        if (target == null || !defenderId.equals(targetController)
                || !predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.filter())) {
            return;
        }
        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            return;
        }

        stepDepartureSupport.destroyThenContinue(gameData, entry, effect, targetId, false, this);
    }

    /**
     * The no-combat-damage instruction is a separate step after the destroy instruction (CR 608.2c). It is
     * reached only when the artifact was destroyed: an indestructible artifact can't be destroyed (CR 702.12b), so the
     * "if you do" condition is not met and the handler returns before the destruction. The no-damage restriction is
     * also recorded as unpreventable, so later damage prevention can't restore combat damage.
     */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            gameData.creaturesPreventedFromDealingCombatDamage.add(sourcePermanentId);
            gameData.creaturesAssigningNoCombatDamageThisTurn.add(sourcePermanentId);
            gameLogService.append(gameData,
                    GameLog.cardThen(source.getCard(), " assigns no combat damage this turn."));
        }
    }
}
