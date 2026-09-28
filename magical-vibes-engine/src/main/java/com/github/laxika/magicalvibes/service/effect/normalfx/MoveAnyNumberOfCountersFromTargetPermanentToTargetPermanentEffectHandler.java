package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a choice of any number of counters of each kind between two permanents. */
@Component
@RequiredArgsConstructor
public class MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent destination = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (source == null || destination == null || source == destination) {
            return;
        }

        List<CounterType> counterTypes = source.getCounters().entrySet().stream()
                .filter(entryValue -> entryValue.getKey() != CounterType.ANY
                        && entryValue.getKey() != CounterType.SILVER
                        && entryValue.getValue() > 0)
                .map(java.util.Map.Entry::getKey)
                .toList();
        if (counterTypes.isEmpty()) {
            return;
        }

        playerInputService.beginMoveAnyNumberOfCountersAmountChoice(
                gameData,
                entry.getControllerId(),
                source.getId(),
                destination.getId(),
                counterTypes,
                0,
                entry.getCard().getName(),
                source.getCounterCount(counterTypes.getFirst()));
    }
}
