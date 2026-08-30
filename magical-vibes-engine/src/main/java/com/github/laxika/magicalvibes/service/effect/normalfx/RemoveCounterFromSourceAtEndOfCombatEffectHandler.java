package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterFromSourceAtEndOfCombatEffect}: schedule the source permanent to have
 * one counter of the effect's type removed at end of combat via
 * {@link RemoveCounterFromSourceAtEndOfCombat}. E.g. Clockwork Beast's "At end of combat, if this
 * creature attacked or blocked this combat, remove a +1/+0 counter from it".
 */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceAtEndOfCombatEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }
        if (gameData.currentStep == TurnStep.END_OF_COMBAT) {
            permanentCounterSupport.removeCounterFromPermanent(gameData, self, e.counterType(), 1);
            return;
        }
        gameData.queueDelayedAction(new RemoveCounterFromSourceAtEndOfCombat(self.getId(), e.counterType(), 1));
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " will have a counter removed at end of combat."));
    }
}
