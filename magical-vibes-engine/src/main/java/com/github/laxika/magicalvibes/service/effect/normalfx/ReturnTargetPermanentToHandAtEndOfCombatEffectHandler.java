package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ReturnTargetPermanentToHandAtEndOfCombatEffect} by scheduling each permanent its subject names for
 * a delayed return to its owner's hand at end of combat.
 */
@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetPermanentToHandAtEndOfCombatEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            DelayedPermanentActions.schedule(gameData, entry,
                    new DelayedPermanentAction(permanent.getId(),
                            DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT),
                    ActivePlayer.ANY);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    " will be returned to its owner's hand at end of combat."));
        }
    }
}
