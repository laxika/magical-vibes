package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the active player's combat damage assignment for one multi-blocked (or
 * trample/unblocked-overflow) attacker. The notification derives its target views from the
 * record's domain targets — the legacy begin site built both lists pairwise from the same
 * data, so begin and reconnect replay are byte-identical. The answer delegates to the
 * legacy combat flow untouched: {@link CombatService#handleCombatDamageAssigned} validates
 * against the combat state on {@code GameData} (pending indices, assignment math — NOT this
 * record, preserving the legacy tolerance for out-of-order answers), an invalid assignment
 * re-sends the prompt via {@code resolveCombatDamage} before rethrowing, and a valid one
 * feeds the damage-resolution loop, which begins a fresh record for the next attacker.
 */
@Component
@RequiredArgsConstructor
public class CombatDamageAssignmentInteractionHandler
        implements InteractionHandler<PendingInteraction.CombatDamageAssignment> {

    private final SessionManager sessionManager;
    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.CombatDamageAssignment> handledType() {
        return PendingInteraction.CombatDamageAssignment.class;
    }

    @Override
    public AwaitingInput legacyInputType() {
        return AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CombatDamageAssigned.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.CombatDamageAssignment interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.CombatDamageAssignment interaction, UUID recipientId) {
        List<CombatDamageTargetView> targetViews = interaction.validTargets().stream()
                .map(t -> new CombatDamageTargetView(
                        t.id().toString(), t.name(), t.effectiveToughness(), t.currentDamage(), t.isPlayer()))
                .toList();
        sessionManager.sendToPlayer(recipientId, new CombatDamageAssignmentNotification(
                interaction.attackerIndex(), interaction.attackerPermanentId().toString(),
                interaction.attackerName(), interaction.totalDamage(), targetViews,
                interaction.isTrample(), interaction.isDeathtouch()));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.CombatDamageAssignment interaction,
                             InteractionAnswer answer) {
        InteractionAnswer.CombatDamageAssigned assigned = (InteractionAnswer.CombatDamageAssigned) answer;
        applyAssignment(gameData, player, assigned.attackerIndex(), assigned.assignments(),
                combatService, turnProgressionService);
    }

    /**
     * The legacy {@code GameService.handleCombatDamageAssigned} body: apply the assignment,
     * re-send the prompt on an invalid one, then continue the damage-resolution loop.
     * Shared with the {@code GameService} stray-message fallback (a request arriving while
     * no assignment is active took this exact path in the legacy code too).
     */
    public static void applyAssignment(GameData gameData, Player player, int attackerIndex,
                                       Map<UUID, Integer> assignments,
                                       CombatService combatService,
                                       TurnProgressionService turnProgressionService) {
        try {
            combatService.handleCombatDamageAssigned(gameData, player, attackerIndex, assignments);
        } catch (IllegalStateException e) {
            // Re-send the assignment notification so the player can retry
            // (the frontend already cleared its popup when it sent the invalid request)
            combatService.resolveCombatDamage(gameData);
            throw e;
        }
        turnProgressionService.handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
    }
}
