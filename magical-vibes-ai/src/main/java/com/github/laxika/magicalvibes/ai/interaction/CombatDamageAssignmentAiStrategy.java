package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Answers combat damage assignments: lethal damage to each blocker in presented order, any
 * remainder to the overflow player target (else piled onto the first blocker). Ported
 * verbatim from the legacy {@code AiChoiceHandler.handleCombatDamageAssignment} heuristic.
 */
@Slf4j
class CombatDamageAssignmentAiStrategy implements AiInteractionStrategy<PendingInteraction.CombatDamageAssignment> {

    @Override
    public Class<PendingInteraction.CombatDamageAssignment> handledType() {
        return PendingInteraction.CombatDamageAssignment.class;
    }

    @Override
    public void answer(PendingInteraction.CombatDamageAssignment interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            log.warn("AI: No combat damage assignment context for player {} in game {} (cda={})",
                    ctx.aiPlayerId(), ctx.gameId(), interaction);
            return;
        }

        int atkIdx = interaction.attackerIndex();
        int totalDamage = interaction.totalDamage();
        var targets = interaction.validTargets();

        Map<String, Integer> assignments = new HashMap<>();
        int remaining = totalDamage;

        for (var target : targets) {
            if (target.isPlayer()) continue;
            int lethal = target.effectiveToughness() - target.currentDamage();
            int dmg = Math.min(remaining, lethal);
            if (dmg > 0) {
                assignments.put(target.id().toString(), dmg);
                remaining -= dmg;
            }
        }

        if (remaining > 0) {
            for (var target : targets) {
                if (target.isPlayer()) {
                    assignments.put(target.id().toString(), remaining);
                    remaining = 0;
                    break;
                }
            }
        }

        if (remaining > 0 && !targets.isEmpty()) {
            var firstBlocker = targets.stream().filter(t -> !t.isPlayer()).findFirst().orElse(targets.get(0));
            assignments.merge(firstBlocker.id().toString(), remaining, Integer::sum);
        }

        log.info("AI: Assigning combat damage for attacker {} in game {}: {}", atkIdx, ctx.gameId(), assignments);
        ctx.gameActions().handleCombatDamageAssigned(ctx.selfConnection(),
                new CombatDamageAssignedRequest(atkIdx, assignments));
    }
}
