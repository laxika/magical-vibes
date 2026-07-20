package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersFromTargetPermanentEffect}: removes every counter of the given
 * type from the targeted permanent. No-op when the target is gone or carries none of that type.
 */
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersFromTargetPermanentEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int removed = target.getCounterCount(e.counterType());
        if (removed > 0) {
            target.setCounterCount(e.counterType(), 0);
            String counterName = permanentCounterSupport.counterTypeName(e.counterType());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(target.getCard()).text(" removes all its " + counterName + " counters (" + removed + ").").build());
        }
    }
}
