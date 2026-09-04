package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect}
 * after its permanent target and the wrapped may choice have both been made.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect) effect;
        UUID targetId = entry.getTargetId();
        UUID defenderId = entry.getAttackedTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID controllerId = entry.getControllerId();
        if (targetId == null || defenderId == null || sourcePermanentId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID targetController = gameQueryService.findPermanentController(gameData, targetId);
        if (target == null || !defenderId.equals(targetController)
                || !predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.filter())) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        UUID sourceController = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        if (e.duration().isSourceLinked() && (source == null || !controllerId.equals(sourceController))) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability has no effect (source left the battlefield or changed controller)."));
            return;
        }

        creatureControlService.applyControlEffect(gameData, controllerId, target,
                new GainControlOfTargetEffect(e.duration()), e.duration().toEffectDuration(),
                e.duration().isSourceLinked() ? sourcePermanentId : null, entry.getCard().getName());
        if (source != null) {
            gameData.creaturesPreventedFromDealingCombatDamage.add(sourcePermanentId);
            gameLogService.append(gameData,
                    GameLog.cardThen(source.getCard(), " assigns no combat damage this turn."));
        }
    }
}
