package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneCounterOnSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutMinusOneCounterOnSourceAtEndOfCombatEffect}: schedule the source permanent to
 * receive a -1/-1 counter at end of combat via {@link PutMinusOneCounterAtEndOfCombat}. E.g. Wicker
 * Warcrawler's "Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of
 * combat".
 */
@Component
@RequiredArgsConstructor
public class PutMinusOneCounterOnSourceAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMinusOneCounterOnSourceAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }
        gameData.queueDelayedAction(new PutMinusOneCounterAtEndOfCombat(self.getId(), 1));
        String logEntry = self.getCard().getName() + " will get a -1/-1 counter at end of combat.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }
}
