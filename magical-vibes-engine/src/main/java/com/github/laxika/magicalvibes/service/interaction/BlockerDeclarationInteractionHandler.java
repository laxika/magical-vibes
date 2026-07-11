package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles the defending player's blocker declaration. The available-blockers notification is
 * re-derived from live combat state at prompt time using the full begin-site filter set
 * (the legacy reconnect replay applied only the plain "can't be blocked" filter — a
 * documented replay-fidelity correction); the answer runs the legacy combat flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerDeclarationInteractionHandler
        implements InteractionHandler<PendingInteraction.BlockerDeclaration> {

    private final SessionManager sessionManager;
    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.BlockerDeclaration> handledType() {
        return PendingInteraction.BlockerDeclaration.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.BlockersDeclared.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.BlockerDeclaration interaction) {
        return interaction.defenderId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.BlockerDeclaration interaction, UUID recipientId) {
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = interaction.defenderId();
        List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = combatService.getBlockableAttackerIndices(gameData, activeId, defenderId);
        sessionManager.sendToPlayer(recipientId, combatService.buildAvailableBlockersMessage(
                gameData, blockable, attackerIndices, defenderId, activeId));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.BlockerDeclaration interaction,
                             InteractionAnswer answer) {
        InteractionAnswer.BlockersDeclared declared = (InteractionAnswer.BlockersDeclared) answer;
        turnProgressionService.handleCombatResult(
                combatService.declareBlockers(gameData, player, declared.blockerAssignments()), gameData);
    }
}
