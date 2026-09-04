package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect}
 * after its permanent target and the wrapped payment choice have both been made.
 */
@Component
@RequiredArgsConstructor
public class DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

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

        if (permanentRemovalService.tryDestroyPermanent(gameData, target, false)) {
            gameLogService.append(gameData, GameLog.isDestroyed(target.getCard()));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            gameData.creaturesPreventedFromDealingCombatDamage.add(sourcePermanentId);
            gameLogService.append(gameData,
                    GameLog.cardThen(source.getCard(), " assigns no combat damage this turn."));
        }
    }
}
