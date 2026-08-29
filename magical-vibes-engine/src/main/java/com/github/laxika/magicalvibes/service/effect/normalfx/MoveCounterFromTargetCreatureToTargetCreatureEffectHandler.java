package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromTargetCreatureToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (invalid targets)."));
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent destination = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (source == null || destination == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (target no longer on the battlefield)."));
            return;
        }

        MoveCounterFromTargetCreatureToTargetCreatureEffect moveEffect =
                (MoveCounterFromTargetCreatureToTargetCreatureEffect) effect;
        boolean moveAll = moveEffect.moveAll();

        if (moveEffect.anyNumber()) {
            // "Move any number of [type] counters" (Bioshift) — the controller picks how many, so
            // pause for a number choice; ChoiceHandlerService performs the move on the answer.
            int available = source.getCounterCount(moveEffect.counterType());
            if (available <= 0) {
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " has no counters to move."));
                return;
            }
            playerInputService.beginMoveCountersAmountChoice(gameData, entry.getControllerId(), source.getId(),
                    destination.getId(), moveEffect.counterType(), entry.getCard().getName(), available);
            return;
        }

        if (moveEffect.counterType() != null) {
            moveSingleCounter(gameData, entry, source, destination, moveEffect.counterType());
            return;
        }

        if (moveAll) {
            // "Move all counters" — move every counter of every kind.
            List<CounterType> kinds = source.getCounters().entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .toList();
            if (kinds.isEmpty()) {
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " has no counters to move."));
                return;
            }
            for (CounterType kind : kinds) {
                int count = source.getCounterCount(kind);
                source.setCounterCount(kind, 0);
                destination.setCounterCount(kind, destination.getCounterCount(kind) + count);
            }
            gameLogService.append(gameData, GameLog.builder().text("All counters are moved from ").card(source.getCard()).text(" onto ").card(destination.getCard()).text(".").build());
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
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " has no counters to move."));
            return;
        }

        source.setCounterCount(toMove, source.getCounterCount(toMove) - 1);
        destination.setCounterCount(toMove, destination.getCounterCount(toMove) + 1);

        gameLogService.append(gameData, GameLog.builder().text("A counter is moved from ").card(source.getCard()).text(" onto ").card(destination.getCard()).text(".").build());
        log.info("Game {} - {} moves a {} counter from {} to {}", gameData.id, entry.getCard().getName(),
                toMove, source.getCard().getName(), destination.getCard().getName());
    }

    private void moveSingleCounter(GameData gameData, StackEntry entry, Permanent source,
                                    Permanent destination, CounterType counterType) {
        if (source == destination
                || source.getCounterCount(counterType) <= 0
                || gameQueryService.cantHaveCounters(gameData, destination)
                || (counterType == CounterType.PLUS_ONE_PLUS_ONE
                && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, destination))) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - 1);
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, destination, counterType, 1);

        gameLogService.append(gameData, GameLog.builder().text("A ")
                .text(counterType.name().toLowerCase()).text(" counter is moved from ")
                .card(source.getCard()).text(" onto ").card(destination.getCard()).text(".").build());
        log.info("Game {} - {} moves a {} counter from {} to {}", gameData.id, entry.getCard().getName(),
                counterType, source.getCard().getName(), destination.getCard().getName());
    }
}
