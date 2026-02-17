package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        if (gameData.awaitingInput != AwaitingInput.PERMANENT_CHOICE) {
            throw new IllegalStateException("Not awaiting permanent choice");
        }
        if (!player.getId().equals(gameData.awaitingPermanentChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = gameData.awaitingPermanentChoiceValidIds;

        gameData.awaitingInput = null;
        gameData.awaitingPermanentChoicePlayerId = null;
        gameData.awaitingPermanentChoiceValidIds = null;

        if (!validIds.contains(permanentId)) {
            throw new IllegalStateException("Invalid permanent: " + permanentId);
        }

        PermanentChoiceContext context = gameData.permanentChoiceContext;
        gameData.permanentChoiceContext = null;

        if (context instanceof PermanentChoiceContext.CloneCopy) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, permanentId);
            if (targetPerm == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            gameHelper.completeCloneEntry(gameData, permanentId);

            // If no legend rule or other awaiting input pending, do SBA + auto-pass
            if (gameData.awaitingInput == null) {
                gameHelper.performStateBasedActions(gameData);

                if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                    gameHelper.processNextDeathTriggerTarget(gameData);
                    if (gameData.awaitingInput != null) {
                        return;
                    }
                }

                if (!gameData.pendingMayAbilities.isEmpty()) {
                    playerInputService.processNextMayAbility(gameData);
                    return;
                }

                turnProgressionService.resolveAutoPass(gameData);
            }
        } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
            Permanent aura = gameQueryService.findPermanentById(gameData, auraGraft.auraPermanentId());
            if (aura == null) {
                throw new IllegalStateException("Aura permanent no longer exists");
            }

            Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
            if (newTarget == null) {
                throw new IllegalStateException("Target permanent no longer exists");
            }

            aura.setAttachedTo(permanentId);

            String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
            // Legend rule: keep chosen permanent, move all others with the same name to graveyard
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            List<Permanent> toRemove = new ArrayList<>();
            for (Permanent perm : battlefield) {
                if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                    toRemove.add(perm);
                }
            }
            for (Permanent perm : toRemove) {
                boolean wasCreature = gameQueryService.isCreature(gameData, perm);
                battlefield.remove(perm);
                gameHelper.addCardToGraveyard(gameData, playerId, perm.getOriginalCard());
                gameHelper.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                if (wasCreature) {
                    gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                }
                String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
            }

            gameHelper.removeOrphanedAuras(gameData);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            UUID sacrificingPlayerId = sacrificeCreature.sacrificingPlayerId();
            gameHelper.removePermanentToGraveyard(gameData, target);

            String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
            String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

            gameHelper.performStateBasedActions(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.BounceCreature bounceCreature) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            UUID bouncingPlayerId = bounceCreature.bouncingPlayerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(bouncingPlayerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameHelper.removeOrphanedAuras(gameData);
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), bouncingPlayerId);
                gameData.stolenCreatures.remove(target.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by Sunken Hope", gameData.id, target.getCard().getName());
            }

            gameHelper.performStateBasedActions(gameData);

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.CopySpellRetarget retarget) {
            StackEntry copyEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(retarget.copyCardId())) {
                    copyEntry = se;
                    break;
                }
            }
            if (copyEntry == null) {
                log.info("Game {} - Copy no longer on stack for retarget", gameData.id);
            } else {
                copyEntry.setTargetPermanentId(permanentId);
                String logMsg = "Copy of " + copyEntry.getCard().getName() + " now targets " + getTargetDisplayName(gameData, permanentId) + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - Copy retargeted to {}", gameData.id, getTargetDisplayName(gameData, permanentId));
            }

            turnProgressionService.resolveAutoPass(gameData);
        } else if (context instanceof PermanentChoiceContext.DeathTriggerTarget dtt) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                // Create the triggered ability stack entry with the chosen target
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        dtt.dyingCard(),
                        dtt.controllerId(),
                        dtt.dyingCard().getName() + "'s ability",
                        new ArrayList<>(dtt.effects())
                );
                entry.setTargetPermanentId(permanentId);
                gameData.stack.add(entry);

                String logEntry = dtt.dyingCard().getName() + "'s death trigger targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger targets {}", gameData.id, dtt.dyingCard().getName(), target.getCard().getName());
            } else {
                String logEntry = dtt.dyingCard().getName() + "'s death trigger has no valid target.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger target no longer exists", gameData.id, dtt.dyingCard().getName());
            }

            // Process more pending death trigger targets
            if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                gameHelper.processNextDeathTriggerTarget(gameData);
                return;
            }

            // Process pending may abilities
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            gameData.priorityPassedBy.clear();
            turnProgressionService.resolveAutoPass(gameData);
        } else if (gameData.pendingAuraCard != null) {
            Card auraCard = gameData.pendingAuraCard;
            gameData.pendingAuraCard = null;

            Permanent creatureTarget = gameQueryService.findPermanentById(gameData, permanentId);
            if (creatureTarget == null) {
                throw new IllegalStateException("Target creature no longer exists");
            }

            // Create Aura permanent attached to the creature, under controller's control
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(creatureTarget.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = auraCard.getName() + " enters the battlefield from graveyard attached to " + creatureTarget.getCard().getName() + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned {} from graveyard to battlefield attached to {}",
                    gameData.id, playerName, auraCard.getName(), creatureTarget.getCard().getName());

            turnProgressionService.resolveAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending permanent choice context");
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (gameData.awaitingInput != AwaitingInput.MULTI_PERMANENT_CHOICE) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        if (!player.getId().equals(gameData.awaitingMultiPermanentChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = gameData.awaitingMultiPermanentChoiceValidIds;
        int maxCount = gameData.awaitingMultiPermanentChoiceMaxCount;

        gameData.awaitingInput = null;
        gameData.awaitingMultiPermanentChoicePlayerId = null;
        gameData.awaitingMultiPermanentChoiceValidIds = null;
        gameData.awaitingMultiPermanentChoiceMaxCount = 0;

        if (permanentIds == null) {
            permanentIds = List.of();
        }

        if (permanentIds.size() > maxCount) {
            throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
        }

        // Validate no duplicates
        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }

        for (UUID permId : permanentIds) {
            if (!validIds.contains(permId)) {
                throw new IllegalStateException("Invalid permanent: " + permId);
            }
        }

        if (gameData.pendingCombatDamageBounceTargetPlayerId != null) {
            UUID targetPlayerId = gameData.pendingCombatDamageBounceTargetPlayerId;
            gameData.pendingCombatDamageBounceTargetPlayerId = null;

            if (permanentIds.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to return any permanents.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                List<Permanent> targetBattlefield = gameData.playerBattlefields.get(targetPlayerId);
                List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                List<String> bouncedNames = new ArrayList<>();

                for (UUID permId : permanentIds) {
                    Permanent toReturn = null;
                    for (Permanent p : targetBattlefield) {
                        if (p.getId().equals(permId)) {
                            toReturn = p;
                            break;
                        }
                    }
                    if (toReturn != null) {
                        targetBattlefield.remove(toReturn);
                        targetHand.add(toReturn.getCard());
                        bouncedNames.add(toReturn.getCard().getName());
                    }
                }

                if (!bouncedNames.isEmpty()) {
                    gameHelper.removeOrphanedAuras(gameData);
                    String logEntry = String.join(", ", bouncedNames) + (bouncedNames.size() == 1 ? " is" : " are") + " returned to " + gameData.playerIdToName.get(targetPlayerId) + "'s hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            turnProgressionService.advanceStep(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    private String getTargetDisplayName(GameData gameData, UUID targetId) {
        // Check if it's a player
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) return playerName;

        // Check stack entries
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetId)) return se.getCard().getName();
        }

        // Check battlefield permanents
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(targetId)) return p.getCard().getName();
            }
        }

        return targetId.toString();
    }
}
