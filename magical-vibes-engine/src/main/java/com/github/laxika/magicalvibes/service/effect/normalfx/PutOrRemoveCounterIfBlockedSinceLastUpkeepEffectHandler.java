package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect}: adds a counter when the source
 * blocked or was blocked since the controller's last upkeep, otherwise removes one (clamped at zero).
 * The tracking flag is consumed either way, opening a fresh window for the next upkeep.
 */
@Component
@RequiredArgsConstructor
public class PutOrRemoveCounterIfBlockedSinceLastUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        boolean blocked = self.isBlockedOrWasBlockedSinceLastUpkeep();
        self.setBlockedOrWasBlockedSinceLastUpkeep(false);

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        if (blocked) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), 1);
            return;
        }

        int current = self.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }
        self.setCounterCount(e.counterType(), current - 1);
        if (e.counterType() == CounterType.OIL) {
            gameData.recordOilCounterRemoved(self, 1);
        }
        gameLogService.append(gameData, GameLog.builder().card(self.getCard())
                .text(" removes a " + counterName + " counter.").build());
    }
}
