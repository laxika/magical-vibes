package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroySelfAtEndOfCombatEffect}: schedule the source permanent for destruction at
 * end of combat via {@link DelayedPermanentAction} (regeneration/indestructible apply). E.g.
 * Cinder Wall's "When this creature blocks, destroy it at end of combat".
 */
@Component
@RequiredArgsConstructor
public class DestroySelfAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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
        gameData.queueDelayedAction(new DelayedPermanentAction(self.getId(),
                DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(self.getCard(), " will be destroyed at end of combat."));
    }
}
