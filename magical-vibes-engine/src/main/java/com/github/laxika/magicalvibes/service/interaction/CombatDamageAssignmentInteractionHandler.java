package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Handles one manual combat-damage assignment answer. The canonical event subscriber derives
 * the notification from the pending interaction's immutable domain targets, so initial delivery,
 * invalid-answer retry, and reconnect replay are identical.
 */
@Component
@RequiredArgsConstructor
public class CombatDamageAssignmentInteractionHandler
        implements InteractionHandler<PendingInteraction.CombatDamageAssignment> {

    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.CombatDamageAssignment> handledType() {
        return PendingInteraction.CombatDamageAssignment.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CombatDamageAssigned.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.CombatDamageAssignment interaction,
                             InteractionAnswer answer) {
        InteractionAnswer.CombatDamageAssigned assigned =
                (InteractionAnswer.CombatDamageAssigned) answer;
        applyAssignment(
                gameData,
                player,
                assigned.attackerIndex(),
                assigned.assignments(),
                combatService,
                turnProgressionService);
    }

    /** Applies a validated assignment answer and continues the combat damage step. */
    public static void applyAssignment(GameData gameData, Player player, int attackerIndex,
                                       Map<UUID, Integer> assignments,
                                       CombatService combatService,
                                       TurnProgressionService turnProgressionService) {
        combatService.handleCombatDamageAssigned(gameData, player, attackerIndex, assignments);
        turnProgressionService.handleCombatResult(
                combatService.resolveCombatDamage(gameData), gameData);
    }
}
