package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromSourceToOtherCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves a source-counter move by choosing the total amount first, then distributing that amount
 * among any number of other creatures in battlefield order.
 */
@Component
@RequiredArgsConstructor
public class MoveCountersFromSourceToOtherCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCountersFromSourceToOtherCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MoveCountersFromSourceToOtherCreaturesEffect move =
                (MoveCountersFromSourceToOtherCreaturesEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            clearState(gameData);
            return;
        }

        List<Permanent> creatures = eligibleCreatures(gameData, source.getId(), move.counterType());
        if (creatures.isEmpty()) {
            clearState(gameData);
            return;
        }

        if (entry.getEventValue() == 0 && gameData.pendingDividedDamageTargetId == null
                && gameData.pendingETBDamageAssignments.isEmpty()) {
            if (gameData.chosenXValue == null) {
                int available = source.getCounterCount(move.counterType());
                if (available <= 0) {
                    clearState(gameData);
                    return;
                }
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                        entry.getControllerId(), 0, available,
                        entry.getCard().getName() + " - Choose how many counters to move.",
                        entry.getCard().getName()));
                return;
            }

            int chosen = Math.max(0, Math.min(gameData.chosenXValue,
                    source.getCounterCount(move.counterType())));
            gameData.chosenXValue = null;
            if (chosen == 0) {
                clearState(gameData);
                return;
            }
            permanentCounterSupport.removeCounterFromPermanent(
                    gameData, source, move.counterType(), chosen);
            entry.setEventValue(chosen);
        }

        int total = entry.getEventValue();
        if (total <= 0) {
            clearState(gameData);
            return;
        }

        Map<UUID, Integer> assignments = new LinkedHashMap<>(gameData.pendingETBDamageAssignments);
        UUID pendingCreatureId = gameData.pendingDividedDamageTargetId;
        if (pendingCreatureId != null) {
            Integer chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            gameData.pendingDividedDamageTargetId = null;
            if (chosen != null) {
                assignments.put(pendingCreatureId, chosen);
            }
        }

        int remaining = Math.max(0, total - assignedCounters(assignments));
        if (remaining > 0) {
            Permanent nextCreature = nextUnassigned(creatures, assignments);
            if (nextCreature != null) {
                int remainingCreatures = (int) creatures.stream()
                        .filter(creature -> !assignments.containsKey(creature.getId()))
                        .count();
                int minimum = remainingCreatures == 1 ? remaining : 0;
                gameData.pendingETBDamageAssignments = assignments;
                gameData.pendingDividedDamageTargetId = nextCreature.getId();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                        entry.getControllerId(), minimum, remaining,
                        entry.getCard().getName() + " - Choose how many counters to put on "
                                + nextCreature.getCard().getName() + ".",
                        entry.getCard().getName()));
                return;
            }
        }

        for (Permanent creature : creatures) {
            int amount = assignments.getOrDefault(creature.getId(), 0);
            if (amount > 0) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, creature, move.counterType(), amount);
            }
        }
        clearState(gameData);
    }

    private List<Permanent> eligibleCreatures(GameData gameData, UUID sourceId,
                                               CounterType counterType) {
        List<Permanent> eligible = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (!permanent.getId().equals(sourceId)
                        && gameQueryService.isCreature(gameData, permanent)
                        && canHaveCounter(gameData, permanent, counterType)) {
                    eligible.add(permanent);
                }
            }
        }
        return eligible;
    }

    private boolean canHaveCounter(GameData gameData, Permanent permanent, CounterType counterType) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE
                ? !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)
                : !gameQueryService.cantHaveCounters(gameData, permanent);
    }

    private Permanent nextUnassigned(List<Permanent> creatures, Map<UUID, Integer> assignments) {
        return creatures.stream()
                .filter(creature -> !assignments.containsKey(creature.getId()))
                .findFirst()
                .orElse(null);
    }

    private int assignedCounters(Map<UUID, Integer> assignments) {
        return assignments.values().stream()
                .filter(amount -> amount != null && amount > 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private void clearState(GameData gameData) {
        gameData.chosenXValue = null;
        gameData.pendingETBDamageAssignments = Map.of();
        gameData.pendingDividedDamageTargetId = null;
    }
}
