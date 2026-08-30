package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAnyNumberOfCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RemoveAnyNumberOfCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAnyNumberOfCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (gameData.chosenXValue != null) {
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            removeCounters(gameData, target, chosen);
            return;
        }

        int available = target.getCounters().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        if (available <= 0) {
            return;
        }

        String cardName = entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), available,
                "Choose how many counters to remove from " + target.getCard().getName()
                        + " (0-" + available + ").", cardName));
    }

    private void removeCounters(GameData gameData, Permanent target, int requested) {
        int remaining = Math.max(0, requested);
        int removed = 0;
        for (Map.Entry<CounterType, Integer> counter : List.copyOf(target.getCounters().entrySet())) {
            if (remaining == 0) {
                break;
            }
            int count = Math.min(remaining, counter.getValue());
            target.setCounterCount(counter.getKey(), counter.getValue() - count);
            if (counter.getKey() == CounterType.OIL) {
                gameData.recordOilCounterRemoved(target, count);
            }
            remaining -= count;
            removed += count;
        }

        if (removed > 0) {
            gameLogService.append(gameData,
                    GameLog.textCardText(removed + " counter(s) removed from ", target.getCard(), "."));
        }
    }
}
