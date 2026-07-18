package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersFromSelfEffect}: removes every counter of the given type from
 * the source permanent and snapshots the removed count onto the stack entry as its event value,
 * so a later effect on the same entry can reference "that much" via an {@code EventValue} amount.
 */
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersFromSelfEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            entry.setEventValue(0);
            return;
        }

        int removed = self.getCounterCount(e.counterType());
        self.setCounterCount(e.counterType(), 0);
        entry.setEventValue(removed);

        if (removed > 0) {
            String counterName = permanentCounterSupport.counterTypeName(e.counterType());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(self.getCard()).text(" removes all its " + counterName + " counters (" + removed + ").").build());
        }
    }
}
