package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link RemoveAllCountersFromTargetPermanentEffect}, a REMOVE_COUNTERS step that never prompts: removes
 * every counter of every kind from each permanent the effect's subject names ("all permanents" too, Aether Snap), then
 * sends the last-counter-removed event for each kind it had ("when you remove the last [kind] counter from it"), runs
 * the vanishing last-time-counter check and the battle's defense check for it; and removes the named kind (every
 * kind when none is named) from each player the effect names. It records the total removed, the permanents that lost
 * counters, their controllers and the players that lost counters, and the total as the entry's event value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final BattleDefeatSupport battleDefeatSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersFromTargetPermanentEffect) effect;
        int total = 0;
        List<UUID> permanentIds = new ArrayList<>();
        List<UUID> playerIds = new ArrayList<>();
        if (e.subject() != null) {
            for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, e.subject())) {
                int removed = removeAll(gameData, entry, target);
                if (removed > 0) {
                    total += removed;
                    permanentIds.add(target.getId());
                    UUID controllerId = gameData.findControllerOf(target);
                    if (controllerId != null && !playerIds.contains(controllerId)) {
                        playerIds.add(controllerId);
                    }
                }
            }
        }
        if (e.players() != null) {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.players())) {
                int removed = removeAll(gameData, entry, playerId, e.playerCounterType());
                if (removed > 0) {
                    total += removed;
                    if (!playerIds.contains(playerId)) {
                        playerIds.add(playerId);
                    }
                }
            }
        }
        entry.recordStep(StepKind.REMOVE_COUNTERS,
                total == 0 ? StepOutcome.NONE : new StepOutcome(total, permanentIds, playerIds));
        entry.setEventValue(total);
    }

    private int removeAll(GameData gameData, StackEntry entry, Permanent target) {
        int removed = 0;
        int oilRemoved = target.getCounterCount(CounterType.OIL);
        int timeCountersBefore = target.getCounterCount(CounterType.TIME);
        boolean hadDefenseCounters = target.getCounterCount(CounterType.DEFENSE) > 0;
        Map<CounterType, Integer> removedByKind = new EnumMap<>(CounterType.class);
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            int count = target.getCounterCount(counterType);
            removed += count;
            if (count > 0) {
                removedByKind.put(counterType, count);
            }
            target.setCounterCount(counterType, 0);
        }
        gameData.recordOilCounterRemoved(target, oilRemoved);
        removedByKind.forEach((counterType, count) -> permanentCounterSupport.notifyLastCounterRemoved(
                gameData, target, counterType, count, entry.getControllerId()));
        permanentCounterSupport.checkLastTimeCounterRemoved(gameData, target, CounterType.TIME, timeCountersBefore);
        if (hadDefenseCounters) {
            battleDefeatSupport.checkAfterDefenseRemoved(gameData, target);
        }

        if (removed > 0) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" removes all counters from " + FaceDownVisibility.publicName(gameData, target) + " (" + removed + ").")
                    .build());
        }
        log.info("Game {} - {} removes {} counter(s) from {}", gameData.id,
                entry.getCard().getName(), removed, target.getCard().getName());
        return removed;
    }

    private int removeAll(GameData gameData, StackEntry entry, UUID playerId, PlayerCounterType kind) {
        int removed = 0;
        for (PlayerCounterType counterType : PlayerCounterType.values()) {
            if (kind == null || kind == counterType) {
                removed += gameData.removeAllPlayerCounters(playerId, counterType);
            }
        }
        if (removed > 0) {
            String counters = kind == null ? "" : " " + kind.name().toLowerCase();
            gameLogService.append(gameData, GameLog.builder().text(gameData.playerIdToName.get(playerId)
                    + " loses all" + counters + " counters (" + removed + ", ").card(entry.getCard()).text(").").build());
        }
        log.info("Game {} - player {} loses {} counter(s) to {}", gameData.id, playerId, removed,
                entry.getCard().getName());
        return removed;
    }
}
