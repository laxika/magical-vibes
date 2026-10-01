package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelectedPermanentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the delayed return of a permanent selected from a library. */
@Component
@RequiredArgsConstructor
public class ReturnSelectedPermanentToHandAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSelectedPermanentToHandAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnSelectedPermanentToHandAtEndOfCombatEffect returnEffect =
                (ReturnSelectedPermanentToHandAtEndOfCombatEffect) effect;
        Permanent permanent = gameQueryService.findPermanentById(gameData, returnEffect.permanentId());
        if (permanent == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(),
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT));
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                " will be returned to its owner's hand at end of combat."));
    }
}
