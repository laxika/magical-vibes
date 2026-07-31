package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutCounterOnSourceAtEndOfCombatEffect}: schedule the source permanent to receive
 * the given counters (and optionally a token for its controller) at end of combat via
 * {@link PutCounterOnPermanentAtEndOfCombat}. E.g. Kjeldoran Home Guard.
 */
@Component
@RequiredArgsConstructor
public class PutCounterOnSourceAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnSourceAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnSourceAtEndOfCombatEffect counterEffect = (PutCounterOnSourceAtEndOfCombatEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }
        gameData.queueDelayedAction(new PutCounterOnPermanentAtEndOfCombat(
                self.getId(), counterEffect.counterType(), counterEffect.amount(), false,
                counterEffect.tokenForController()));
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(),
                " will get " + counterEffect.amount() + " counter(s) at end of combat."));
    }
}
