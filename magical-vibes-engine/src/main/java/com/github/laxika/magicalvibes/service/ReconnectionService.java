package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconnectionService {

    private final SessionManager sessionManager;
    private final CombatService combatService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        // Registry-managed interactions replay their own prompt
        if (interactionHandlerRegistry.replayPrompt(gameData, playerId)) {
            return;
        }
        InteractionContext context = gameData.interaction.currentContext();
        if (context != null) {
            resendFromContext(gameData, playerId, context);
            return;
        }
        AwaitingInput awaitingInput = gameData.interaction.awaitingInputType();
        if (awaitingInput == null) return;

        switch (awaitingInput) {
            case ATTACKER_DECLARATION -> {
                if (playerId.equals(gameData.activePlayerId)) {
                    List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, playerId);
                    List<Integer> mustAttack = combatService.getMustAttackIndices(gameData, playerId, attackable);
                    int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
                    boolean mustAttackWithAtLeastOne = combatService.isOpponentForcedToAttack(gameData, playerId);
                    sessionManager.sendToPlayer(playerId, new AvailableAttackersMessage(attackable, mustAttack, combatService.buildAvailableTargets(gameData, playerId), taxPerCreature, mustAttackWithAtLeastOne));
                }
            }
            case BLOCKER_DECLARATION -> {
                UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
                if (playerId.equals(defenderId)) {
                    List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
                    List<Integer> attackerIndices = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
                    List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                    attackerIndices = attackerIndices.stream()
                            .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                            .toList();
                    sessionManager.sendToPlayer(defenderId, combatService.buildAvailableBlockersMessage(
                            gameData, blockable, attackerIndices, defenderId, gameData.activePlayerId));
                }
            }
        }
    }

    private void resendFromContext(GameData gameData, UUID playerId, InteractionContext context) {
        switch (context) {
            case InteractionContext.AttackerDeclaration ad -> {
                if (playerId.equals(ad.activePlayerId())) {
                    List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, playerId);
                    List<Integer> mustAttack = combatService.getMustAttackIndices(gameData, playerId, attackable);
                    int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
                    boolean mustAttackWithAtLeastOne = combatService.isOpponentForcedToAttack(gameData, playerId);
                    sessionManager.sendToPlayer(playerId, new AvailableAttackersMessage(attackable, mustAttack, combatService.buildAvailableTargets(gameData, playerId), taxPerCreature, mustAttackWithAtLeastOne));
                }
            }
            case InteractionContext.BlockerDeclaration bd -> {
                UUID defenderId = bd.defenderId();
                if (playerId.equals(defenderId)) {
                    List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
                    List<Integer> attackerIndices = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
                    List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                    attackerIndices = attackerIndices.stream()
                            .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                            .toList();
                    sessionManager.sendToPlayer(defenderId, combatService.buildAvailableBlockersMessage(
                            gameData, blockable, attackerIndices, defenderId, gameData.activePlayerId));
                }
            }
        }
    }
}
