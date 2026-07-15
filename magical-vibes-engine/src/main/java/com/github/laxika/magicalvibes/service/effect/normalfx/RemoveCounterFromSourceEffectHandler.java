package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterFromSourceEffect}: removes up to {@code amount} counters of the given
 * type from the source permanent, clamped at zero.
 */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        int current = self.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }
        int removed = Math.min(current, e.amount());
        self.setCounterCount(e.counterType(), current - removed);

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(self.getCard().getName()
                + " removes " + removed + " " + counterName + " counter(s)."));
    }
}
