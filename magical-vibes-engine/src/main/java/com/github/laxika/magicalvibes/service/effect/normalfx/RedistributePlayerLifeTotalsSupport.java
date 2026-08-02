package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedistributePlayerLifeTotalsSupport {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    public Map<String, Map<UUID, Integer>> buildChoices(GameData gameData) {
        List<UUID> playerIds = List.copyOf(gameData.orderedPlayerIds);
        Map<UUID, Integer> currentLifeTotals = playerIds.stream()
                .collect(Collectors.toMap(id -> id, gameData::getLife, (left, right) -> left, LinkedHashMap::new));

        Map<String, Map<UUID, Integer>> choices = new LinkedHashMap<>();
        Set<String> seenStates = new LinkedHashSet<>();
        addChoice(choices, seenStates, "No change", currentLifeTotals, playerIds);
        buildSubsets(gameData, playerIds, currentLifeTotals, 0, new ArrayList<>(), choices, seenStates);
        return choices;
    }

    public void applyChoice(GameData gameData, String choice,
                            Map<String, Map<UUID, Integer>> choices) {
        Map<UUID, Integer> newLifeTotals = choices.get(choice);
        if (newLifeTotals == null) {
            throw new IllegalStateException("Illegal life-total redistribution choice");
        }

        Map<UUID, Integer> oldLifeTotals = gameData.orderedPlayerIds.stream()
                .collect(Collectors.toMap(id -> id, gameData::getLife, (left, right) -> left, LinkedHashMap::new));
        for (UUID playerId : gameData.orderedPlayerIds) {
            int oldLife = oldLifeTotals.get(playerId);
            int newLife = newLifeTotals.getOrDefault(playerId, oldLife);
            if (oldLife == newLife) {
                continue;
            }
            if (!gameQueryService.canPlayerLifeChange(gameData, playerId)
                    || (newLife > oldLife && !gameQueryService.canPlayerGainLife(gameData, playerId))) {
                throw new IllegalStateException("Life-total redistribution is no longer legal");
            }
        }

        boolean changed = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            int oldLife = oldLifeTotals.get(playerId);
            int newLife = newLifeTotals.getOrDefault(playerId, oldLife);
            if (oldLife == newLife) {
                continue;
            }
            if (lifeSupport.applySetLifeTotal(gameData, playerId, newLife)) {
                changed = true;
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(
                        playerName + "'s life total becomes " + gameData.getLife(playerId) + " (was " + oldLife + ")."));
            }
        }
        if (!changed) {
            gameLogService.append(gameData, GameLog.text("Life totals remain unchanged."));
        }
    }

    private void buildSubsets(GameData gameData, List<UUID> playerIds,
                              Map<UUID, Integer> currentLifeTotals, int index,
                              List<UUID> selected, Map<String, Map<UUID, Integer>> choices,
                              Set<String> seenStates) {
        if (index == playerIds.size()) {
            if (!selected.isEmpty()) {
                List<Integer> lifeTotals = new ArrayList<>(selected.stream().map(currentLifeTotals::get).toList());
                addPermutations(gameData, playerIds, currentLifeTotals, selected, lifeTotals, 0,
                        choices, seenStates);
            }
            return;
        }

        buildSubsets(gameData, playerIds, currentLifeTotals, index + 1, selected, choices, seenStates);
        selected.add(playerIds.get(index));
        buildSubsets(gameData, playerIds, currentLifeTotals, index + 1, selected, choices, seenStates);
        selected.removeLast();
    }

    private void addPermutations(GameData gameData, List<UUID> playerIds,
                                 Map<UUID, Integer> currentLifeTotals, List<UUID> selected,
                                 List<Integer> lifeTotals, int index,
                                 Map<String, Map<UUID, Integer>> choices, Set<String> seenStates) {
        if (index == lifeTotals.size()) {
            Map<UUID, Integer> candidate = new LinkedHashMap<>(currentLifeTotals);
            for (int i = 0; i < selected.size(); i++) {
                candidate.put(selected.get(i), lifeTotals.get(i));
            }
            if (isLegalAssignment(gameData, currentLifeTotals, candidate)) {
                String label = playerIds.stream()
                        .map(id -> gameData.playerIdToName.get(id) + ": " + candidate.get(id))
                        .collect(Collectors.joining("; "));
                addChoice(choices, seenStates, label, candidate, playerIds);
            }
            return;
        }

        for (int i = index; i < lifeTotals.size(); i++) {
            java.util.Collections.swap(lifeTotals, index, i);
            addPermutations(gameData, playerIds, currentLifeTotals, selected, lifeTotals, index + 1,
                    choices, seenStates);
            java.util.Collections.swap(lifeTotals, index, i);
        }
    }

    private boolean isLegalAssignment(GameData gameData, Map<UUID, Integer> currentLifeTotals,
                                      Map<UUID, Integer> candidate) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int oldLife = currentLifeTotals.get(playerId);
            int newLife = candidate.get(playerId);
            if (oldLife == newLife) {
                continue;
            }
            if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                return false;
            }
            if (newLife > oldLife && !gameQueryService.canPlayerGainLife(gameData, playerId)) {
                return false;
            }
        }
        return true;
    }

    private void addChoice(Map<String, Map<UUID, Integer>> choices, Set<String> seenStates,
                           String label, Map<UUID, Integer> candidate, List<UUID> playerIds) {
        String state = playerIds.stream().map(id -> String.valueOf(candidate.get(id)))
                .collect(Collectors.joining(","));
        if (seenStates.add(state)) {
            choices.put(label, Map.copyOf(candidate));
        }
    }
}
