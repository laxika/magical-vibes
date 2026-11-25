package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterFromSourceAtEndOfCombatEffect}: as the end of combat step's own ability it removes one
 * counter of the effect's type from the source permanent (Clockwork Beast's "At end of combat, if this creature
 * attacked or blocked this combat, remove a +1/+0 counter from it"); anywhere else it registers the delayed triggered
 * ability that does (Clockwork Beetle's "Whenever this creature attacks or blocks, remove a +1/+1 counter from it at
 * end of combat").
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
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endOfCombat(), entry.getControllerId(), entry.getCard(), self.getId(), null,
                RemoveCountersEffect.fromSelf(e.counterType(), 1)));
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(
                ), " will have a counter removed at end of combat."));    }
}
