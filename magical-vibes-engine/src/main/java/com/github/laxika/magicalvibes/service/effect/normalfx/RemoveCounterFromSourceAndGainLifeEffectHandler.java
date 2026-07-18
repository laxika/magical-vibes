package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterFromSourceAndGainLifeEffect}: removes one counter of the given type
 * from the source permanent, and the controller gains life only if a counter was actually removed
 * ("If you do").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceAndGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceAndGainLifeEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (self.getCounterCount(e.counterType()) <= 0) {
            // No counter to remove -> "If you do" fails, no life gained.
            return;
        }

        self.setCounterCount(e.counterType(), self.getCounterCount(e.counterType()) - 1);
        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("A " + counterName + " counter removed from ", self.getCard(), "."));
        log.info("Game {} - {} counter removed from {}", gameData.id, e.counterType(), self.getCard().getName());

        lifeSupport.applyGainLife(gameData, entry.getControllerId(), e.lifeGain(), null,
                entry.getCard(), entry.getEntryType());
    }
}
