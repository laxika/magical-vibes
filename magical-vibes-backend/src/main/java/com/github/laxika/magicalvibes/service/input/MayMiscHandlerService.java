package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayMiscHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final SessionManager sessionManager;

    public void handleEquipmentAttachChoice(GameData gameData, Player player, boolean accepted,
                                             UUID equipId, UUID targetId) {
        gameData.interaction.clearPendingEquipmentAttach();
        if (accepted) {
            Permanent equipPerm = gameQueryService.findPermanentById(gameData, equipId);
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (equipPerm != null && targetPerm != null) {
                equipPerm.setAttachedTo(targetPerm.getId());
                String attachLog = equipPerm.getCard().getName() + " is attached to " + targetPerm.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, attachLog);
                log.info("Game {} - {} attached to {}", gameData.id, equipPerm.getCard().getName(), targetPerm.getCard().getName());
            }
        } else {
            String declineLog = player.getUsername() + " declines to attach the Equipment.";
            gameBroadcastService.logAndBroadcast(gameData, declineLog);
            log.info("Game {} - {} declines equipment attachment", gameData.id, player.getUsername());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleMayNotUntapChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        // Find the permanent on the battlefield by Card identity
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        if (accepted && sourcePermanent != null) {
            sourcePermanent.untap();
            String logEntry = player.getUsername() + " untaps " + sourceCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} untaps {} (may-not-untap choice)", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " chooses not to untap " + sourceCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} keeps {} tapped (may-not-untap choice)", gameData.id, player.getUsername(), sourceCard.getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            // All may-not-untap choices resolved — complete the turn advance and resume auto-pass
            turnProgressionService.completeTurnAdvance(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void handleOpeningHandDelayedCounterTrigger(GameData gameData, Player player, boolean accepted,
                                                        PendingMayAbility ability, RegisterDelayedCounterTriggerEffect effect) {
        if (accepted) {
            gameData.openingHandRevealTriggers.add(new OpeningHandRevealTrigger(
                    ability.controllerId(), ability.sourceCard(),
                    new CounterUnlessPaysEffect(effect.genericManaAmount())
            ));

            String logEntry = player.getUsername() + " reveals " + ability.sourceCard().getName() + " from their opening hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} reveals {} from opening hand (delayed counter trigger registered)",
                    gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " declines to reveal " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to reveal {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleOpeningHandDelayedManaTrigger(GameData gameData, Player player, boolean accepted,
                                                     PendingMayAbility ability, RegisterDelayedManaTriggerEffect effect) {
        if (accepted) {
            gameData.openingHandManaTriggers.add(new OpeningHandRevealTrigger(
                    ability.controllerId(), ability.sourceCard(),
                    new AwardManaEffect(effect.color(), effect.amount())
            ));

            String logEntry = player.getUsername() + " reveals " + ability.sourceCard().getName() + " from their opening hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} reveals {} from opening hand (delayed mana trigger registered)",
                    gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " declines to reveal " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to reveal {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSingleDrawReplacementChoice(GameData gameData, Player player, boolean accepted,
                                                   PendingMayAbility ability,
                                                   ReplaceSingleDrawEffect effect) {
        UUID drawingPlayerId = effect.playerId();
        String playerName = gameData.playerIdToName.get(drawingPlayerId);

        if (!accepted) {
            drawService.resolveDrawCardWithoutStaticReplacementCheck(gameData, drawingPlayerId);
            String logEntry = player.getUsername() + " declines to use " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.ABUNDANCE) {
            gameData.interaction.beginColorChoice(
                    drawingPlayerId,
                    null,
                    null,
                    new ColorChoiceContext.DrawReplacementChoice(drawingPlayerId, effect.kind())
            );
            sessionManager.sendToPlayer(drawingPlayerId, new ChooseColorMessage(
                    List.of("LAND", "NONLAND"),
                    "Choose land or nonland for Abundance."
            ));
            log.info("Game {} - Awaiting {} to choose land or nonland for Abundance", gameData.id, playerName);
            return;
        }

        throw new IllegalStateException("Unsupported draw replacement kind: " + effect.kind());
    }

    public void handleMaySacrificeArtifactForDividedDamage(GameData gameData, Player player,
                                                            boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            UUID controllerId = ability.controllerId();
            List<UUID> validArtifactIds = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (gameQueryService.isArtifact(p)) {
                        validArtifactIds.add(p.getId());
                    }
                }
            }

            if (validArtifactIds.isEmpty()) {
                String logEntry = player.getUsername() + " has no artifacts to sacrifice.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} has no artifacts to sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());

                gameData.pendingETBDamageAssignments = Map.of();
                playerInputService.processNextMayAbility(gameData);
                if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                    gameData.priorityPassedBy.clear();
                    gameBroadcastService.broadcastGameState(gameData);
                    turnProgressionService.resolveAutoPass(gameData);
                }
                return;
            }

            Map<UUID, Integer> damageAssignments = gameData.pendingETBDamageAssignments;
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.SacrificeArtifactForDividedDamage(
                            controllerId, ability.sourceCard(), damageAssignments));
            playerInputService.beginPermanentChoice(gameData, controllerId, validArtifactIds,
                    ability.sourceCard().getName() + " — Choose an artifact to sacrifice.");

            String logEntry = player.getUsername() + " accepts — choosing an artifact to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            gameData.pendingETBDamageAssignments = Map.of();

            String logEntry = player.getUsername() + " declines to sacrifice an artifact for " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
        }
    }
}
