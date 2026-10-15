package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneCounterOnSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutMinusOneCounterOnSourceAtEndOfCombatEffect}: creates a delayed "at end of
 * combat" trigger that puts a -1/-1 counter on the source permanent. E.g. Wicker Warcrawler's
 * "Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of combat".
 */
@Component
@RequiredArgsConstructor
public class PutMinusOneCounterOnSourceAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

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
        gameData.queueDelayedAction(new DelayedEndOfCombatTrigger(entry.getControllerId(), entry.getCard(),
                self.getId(), new PutCountersOnSelfEffect(CounterType.MINUS_ONE_MINUS_ONE)));
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " will get a -1/-1 counter at end of combat."));
    }
}
