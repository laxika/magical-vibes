package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DistributeCountersAmongTargetsEffect} for both division modes.
 *
 * <p>EVEN splits {@code total} evenly across the effect's chosen target group and places
 * {@code floor(total / targetCount)} counters on each surviving target; CHOSEN reads the
 * controller-announced per-target amounts from {@code StackEntry.damageAssignments}. Both route
 * placement through {@link PermanentCounterSupport#placeCounterOnPermanent} so counter-type-specific
 * behaviour (-1/-1 prevention/reduction, +1/+1 triggers) is preserved.
 */
@Component
@RequiredArgsConstructor
public class DistributeCountersAmongTargetsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistributeCountersAmongTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DistributeCountersAmongTargetsEffect) effect;

        if (e.etbAssignments()) {
            Map<UUID, Integer> assignments = gameData.pendingETBDamageAssignments;
            gameData.pendingETBDamageAssignments = Map.of();
            applyAssignments(gameData, entry, e, assignments);
            return;
        }

        Map<UUID, Integer> assignments = e.mode() == DivisionMode.CHOSEN
                ? chosenAssignments(entry)
                : evenAssignments(gameData, entry, e);

        applyAssignments(gameData, entry, e, assignments);
    }

    private void applyAssignments(
            GameData gameData,
            StackEntry entry,
            DistributeCountersAmongTargetsEffect e,
            Map<UUID, Integer> assignments) {
        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null) {
                continue; // Partially resolves — skip targets that left the battlefield.
            }
            // An announced target that is no longer a creature at resolution is illegal and isn't
            // affected (CR 608.2b) — never put the counters on an animated land that reverted.
            if (assignment.getValue() == null || assignment.getValue() <= 0) {
                continue;
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, e.counterType(), assignment.getValue());
            if (e.removeAtNextCleanup()) {
                // Bounty of the Hunt's delayed rider: one counter comes back off per counter put on
                // this way. TurnCleanupService sheds them, clamped to what the creature still has.
                target.getCountersToRemoveAtNextCleanup()
                        .merge(e.counterType(), assignment.getValue(), Integer::sum);
            }
        }
    }

    private Map<UUID, Integer> chosenAssignments(StackEntry entry) {
        Map<UUID, Integer> announced = entry.getDamageAssignments();
        if (announced == null || announced.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> assignment : announced.entrySet()) {
            if (entry.isAssignmentTargetLegal(assignment.getKey())) {
                assignments.put(assignment.getKey(), assignment.getValue());
            }
        }
        return assignments;
    }

    private Map<UUID, Integer> evenAssignments(
            GameData gameData, StackEntry entry, DistributeCountersAmongTargetsEffect e) {
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (targetIds.isEmpty()) {
            return Map.of();
        }
        int total = amountEvaluationService.evaluate(gameData, e.total(),
                AmountContext.forStackEntry(entry, null));
        int countPerTarget = total / targetIds.size();
        if (countPerTarget <= 0) {
            return Map.of();
        }
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        for (UUID targetId : targetIds) {
            assignments.put(targetId, countPerTarget);
        }
        return assignments;
    }
}
