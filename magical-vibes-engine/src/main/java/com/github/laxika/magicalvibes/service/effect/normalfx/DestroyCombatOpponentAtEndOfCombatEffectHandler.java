package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link DestroyCombatOpponentAtEndOfCombatEffect}: if the referenced combat opponent
 * (carried as the stack entry's target) is a creature matching the effect's filter, schedule it
 * for destruction at end of combat via {@link DelayedPermanentAction}. Basilisk-style
 * "destroy that creature at end of combat" triggers (e.g. Deathgazer).
 */
@Component
@RequiredArgsConstructor
public class DestroyCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyCombatOpponentAtEndOfCombatEffect destroyEffect = (DestroyCombatOpponentAtEndOfCombatEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, target, destroyEffect.filter())) {
            return;
        }

        gameData.queueDelayedAction(new DelayedPermanentAction(targetId,
                DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT, destroyEffect.cannotBeRegenerated()));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " will be destroyed at end of combat."));
    }
}
