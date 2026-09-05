package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link DestroyCombatOpponentAtEndOfCombatEffect} by scheduling the combat opponent
 * captured when the ability triggered for destruction at end of combat.
 */
@Component
@RequiredArgsConstructor
public class DestroyCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

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
        if (target == null) {
            return;
        }
        if (destroyEffect.putCounterOnSourceIfDestroyed()) {
            gameData.queueDelayedAction(new DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource(
                    targetId,
                    entry.getTriggeringPermanentId(),
                    entry.getControllerId(),
                    entry.getCard(),
                    destroyEffect.cannotBeRegenerated()));
        } else {
            gameData.queueDelayedAction(new DelayedPermanentAction(targetId,
                    DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT, destroyEffect.cannotBeRegenerated()));
        }
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " will be destroyed at end of combat."));
    }
}
