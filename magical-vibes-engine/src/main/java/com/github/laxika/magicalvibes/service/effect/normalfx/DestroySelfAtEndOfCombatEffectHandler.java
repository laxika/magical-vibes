package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroySelfAtEndOfCombatEffect}: schedule the source permanent for destruction at
 * end of combat via {@link DelayedPermanentAction} (regeneration/indestructible apply). E.g.
 * Cinder Wall's "When this creature blocks, destroy it at end of combat". The delayed trigger is controlled
 * by this ability's controller.
 */
@Component
@RequiredArgsConstructor
public class DestroySelfAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroySelfAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }
        // The delayed trigger stays under the control of the player who controlled this ability, even if
        // the permanent changes control before end of combat (CR 603.7e). Sharing the ability's source lets
        // "destroy it and this creature at end of combat" (Goblin Sappers) form one delayed trigger.
        gameData.queueDelayedAction(new DelayedPermanentAction(self.getId(),
                DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT, false, null, null, null, false, entry.getCard(),
                entry.getControllerId(), entry.getSourcePermanentId()));
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " will be destroyed at end of combat."));
    }
}
