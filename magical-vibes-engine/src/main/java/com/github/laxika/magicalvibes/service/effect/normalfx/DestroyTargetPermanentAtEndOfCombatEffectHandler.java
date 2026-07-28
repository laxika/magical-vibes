package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyTargetPermanentAtEndOfCombatEffect}: schedule the targeted permanent for
 * destruction at end of combat via {@link DelayedPermanentAction} (regeneration/indestructible
 * apply). E.g. Goblin Sappers' "Destroy it at end of combat".
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyTargetPermanentAtEndOfCombatEffect destroy = (DestroyTargetPermanentAtEndOfCombatEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        gameData.queueDelayedAction(new DelayedPermanentAction(target.getId(),
                DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT, destroy.cannotBeRegenerated()));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " will be destroyed at end of combat."));
    }
}
