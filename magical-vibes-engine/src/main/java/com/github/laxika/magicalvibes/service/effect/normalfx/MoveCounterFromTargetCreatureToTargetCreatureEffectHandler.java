package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link MoveCounterFromTargetCreatureToTargetCreatureEffect}: removes one counter from the
 * first target creature and places it on the second. Reads the two targets from the ability's flat
 * multi-target list (position 0 = source of the counter, position 1 = destination).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MoveCounterFromTargetCreatureToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromTargetCreatureToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName() + "'s ability fizzles (invalid targets)."));
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent destination = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (source == null || destination == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName() + "'s ability fizzles (target no longer on the battlefield)."));
            return;
        }

        boolean moveAll = ((MoveCounterFromTargetCreatureToTargetCreatureEffect) effect).moveAll();

        if (moveAll) {
            // "Move all counters" — move every counter of every kind.
            List<CounterType> kinds = source.getCounters().entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .toList();
            if (kinds.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(source.getCard().getName() + " has no counters to move."));
                return;
            }
            for (CounterType kind : kinds) {
                int count = source.getCounterCount(kind);
                source.setCounterCount(kind, 0);
                destination.setCounterCount(kind, destination.getCounterCount(kind) + count);
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text("All counters are moved from " + source.getCard().getName() + " onto " + destination.getCard().getName() + "."));
            log.info("Game {} - {} moves all counters from {} to {}", gameData.id, entry.getCard().getName(),
                    source.getCard().getName(), destination.getCard().getName());
            return;
        }

        // "A counter" — move the first kind of counter present on the source creature.
        CounterType toMove = source.getCounters().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (toMove == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(source.getCard().getName() + " has no counters to move."));
            return;
        }

        source.setCounterCount(toMove, source.getCounterCount(toMove) - 1);
        destination.setCounterCount(toMove, destination.getCounterCount(toMove) + 1);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text("A counter is moved from " + source.getCard().getName() + " onto " + destination.getCard().getName() + "."));
        log.info("Game {} - {} moves a {} counter from {} to {}", gameData.id, entry.getCard().getName(),
                toMove, source.getCard().getName(), destination.getCard().getName());
    }
}
