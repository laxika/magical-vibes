package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveDelayCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link RemoveDelayCounterFromExiledSpellEffect} (Ertai's Meddling's upkeep trigger).
 *
 * <p>Removes one delay counter; when the last one comes off, the card leaves exile and goes back
 * onto the stack with the X value and targets the original spell had. The card is put onto the
 * stack rather than cast, so cast triggers do not fire.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveDelayCounterFromExiledSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveDelayCounterFromExiledSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RemoveDelayCounterFromExiledSpellEffect delay = (RemoveDelayCounterFromExiledSpellEffect) effect;
        UUID cardId = delay.cardId();
        int counters = gameData.exiledCardCounterCount(cardId, CounterType.DELAY);
        if (counters <= 0 || gameData.findExiledCard(cardId) == null) return;

        int remaining = Math.max(0, counters - 1);
        if (remaining > 0) {
            gameData.setExiledCardCounters(cardId, CounterType.DELAY, remaining);
            gameLogService.append(gameData, GameLog.cardThen(delay.originalEntry().getCard(),
                    " loses a delay counter (" + remaining + " left)."));
            return;
        }

        gameData.removeFromExile(cardId);
        gameData.stack.add(new StackEntry(delay.originalEntry()));

        gameLogService.append(gameData, GameLog.cardThen(delay.originalEntry().getCard(),
                " loses its last delay counter and is put back onto the stack."));
        log.info("Game {} - {} returned to the stack from delay-counter exile",
                gameData.id, delay.originalEntry().getCard().getName());
    }
}
