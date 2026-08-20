package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a non-targeting counter distribution by prompting for each controlled creature in
 * battlefield order. Choosing zero leaves a creature out of the distribution; the last available
 * creature must receive the remaining counters so the complete total is distributed.
 */
@Component
@RequiredArgsConstructor
public class DistributeCountersAmongControlledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistributeCountersAmongControlledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DistributeCountersAmongControlledCreaturesEffect) effect;
        List<Permanent> creatures = controlledCreatures(gameData, entry.getControllerId());
        int total = amountEvaluationService.evaluate(gameData, e.total(),
                AmountContext.forStackEntry(entry, null));

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

        if (total <= 0 || creatures.isEmpty()) {
            clearState(gameData);
            return;
        }

        int assigned = assignedCounters(assignments);
        int remaining = Math.max(0, total - assigned);
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
                        entry.getCard().getName() + " — Choose how many counters to put on "
                                + nextCreature.getCard().getName() + ".",
                        entry.getCard().getName()));
                return;
            }
        }

        for (Permanent creature : creatures) {
            int amount = assignments.getOrDefault(creature.getId(), 0);
            if (amount > 0) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, creature, e.counterType(), amount);
            }
        }
        clearState(gameData);
    }

    private List<Permanent> controlledCreatures(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
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
        gameData.pendingETBDamageAssignments = Map.of();
        gameData.pendingDividedDamageTargetId = null;
    }
}
