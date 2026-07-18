package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.CombatDamageAssignmentHeuristic;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Answers combat damage assignments by killing the most threatening killable creatures first
 * (by effective power), then sending any legal remainder to the overflow player target (trample)
 * or piling it onto the top remaining threat.
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

        Map<UUID, Integer> uuidAssignments = CombatDamageAssignmentHeuristic.assign(
                interaction, ctx.gameData(), ctx.gameQueryService());

        Map<String, Integer> assignments = new HashMap<>();
        for (var entry : uuidAssignments.entrySet()) {
            assignments.put(entry.getKey().toString(), entry.getValue());
        }

        log.info("AI: Assigning combat damage for attacker {} in game {}: {}",
                interaction.attackerIndex(), ctx.gameId(), assignments);
        ctx.gameActions().handleCombatDamageAssigned(ctx.selfConnection(),
                new CombatDamageAssignedRequest(interaction.attackerIndex(), assignments));
    }
}
