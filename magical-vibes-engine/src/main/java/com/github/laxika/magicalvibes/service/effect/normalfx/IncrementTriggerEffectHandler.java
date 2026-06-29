package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncrementTriggerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Increment: puts a +1/+1 counter on the source creature if the mana spent on the spell that
 * triggered this ability (snapshotted into the stack entry's xValue) is still greater than the
 * creature's current power or toughness (CR 603.4 re-check).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncrementTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncrementTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        int manaSpent = entry.getXValue();
        if (manaSpent <= self.getEffectivePower() && manaSpent <= self.getEffectiveToughness()) {
            return;
        }
        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, self.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + 1);

        String logEntry = self.getCard().getName() + " gets a +1/+1 counter (Increment).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a +1/+1 counter from Increment (mana spent {})",
                gameData.id, self.getCard().getName(), manaSpent);
    }
}
