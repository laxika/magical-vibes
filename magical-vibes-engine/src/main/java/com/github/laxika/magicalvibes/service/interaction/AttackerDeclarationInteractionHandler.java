package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles the active player's attacker declaration. The available-attackers notification is
 * re-derived from live combat state at prompt time (as both the legacy begin site and the
 * legacy reconnect replay did); the answer runs the legacy combat flow, including the
 * re-send-and-rethrow on an invalid declaration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttackerDeclarationInteractionHandler
        implements InteractionHandler<PendingInteraction.AttackerDeclaration> {

    private final SessionManager sessionManager;
    private final CombatService combatService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.AttackerDeclaration> handledType() {
        return PendingInteraction.AttackerDeclaration.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.AttackersDeclared.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.AttackerDeclaration interaction) {
        return interaction.activePlayerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.AttackerDeclaration interaction, UUID recipientId) {
        UUID activeId = interaction.activePlayerId();
        List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, activeId);
        List<Integer> mustAttack = combatService.getMustAttackIndices(gameData, activeId, attackable);
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, activeId);
        boolean mustAttackWithAtLeastOne = combatService.isOpponentForcedToAttack(gameData, activeId);
        sessionManager.sendToPlayer(recipientId, new AvailableAttackersMessage(
                attackable, mustAttack, combatService.buildAvailableTargets(gameData, activeId),
                taxPerCreature, mustAttackWithAtLeastOne));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.AttackerDeclaration interaction,
                             InteractionAnswer answer) {
        InteractionAnswer.AttackersDeclared declared = (InteractionAnswer.AttackersDeclared) answer;
        try {
            turnProgressionService.handleCombatResult(
                    combatService.declareAttackers(gameData, player, declared.attackerIndices(), declared.attackTargets()),
                    gameData);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Re-send available attackers so the player (or AI) can retry
            combatService.handleDeclareAttackersStep(gameData);
            throw e;
        }
    }
}
