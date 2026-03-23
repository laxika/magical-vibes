package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.PendingCapriciousEfreetState;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.DestructionResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentCounterResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles multi-permanent choice inputs where the player selects
 * zero or more permanents from a list.
 *
 * <p>Covers exile-damaged-player-permanent, sacrifice-self-to-destroy,
 * sacrifice attacking creatures, combat damage bounce, awakening counter
 * placement, proliferate, and tap-subtype-boost (e.g. Myr Battlesphere).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiPermanentChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateBasedActionService stateBasedActionService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final DestructionResolutionService destructionResolutionService;
    private final PermanentCounterResolutionService permanentCounterResolutionService;

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MULTI_PERMANENT_CHOICE)) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        InteractionContext.MultiPermanentChoice multiPermanentChoice = gameData.interaction.multiPermanentChoiceContext();
        if (multiPermanentChoice == null || !player.getId().equals(multiPermanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<UUID> validIds = multiPermanentChoice.validIds();
        int maxCount = multiPermanentChoice.maxCount();

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMultiPermanentChoice();

        if (permanentIds == null) {
            permanentIds = List.of();
        }

        if (permanentIds.size() > maxCount) {
            throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
        }

        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }

        for (UUID permId : permanentIds) {
            if (!validIds.contains(permId)) {
                throw new IllegalStateException("Invalid permanent: " + permId);
            }
        }

        if (gameData.pendingExileDamagedPlayerControlsPermanent) {
            handleExileDamagedPlayerControlsPermanent(gameData, playerId, permanentIds);
        } else if (gameData.pendingSacrificeSelfToDestroySourceId != null) {
            handleSacrificeSelfToDestroy(gameData, playerId, permanentIds);
        } else if (gameData.pendingSacrificeAttackingCreature) {
            handleSacrificeAttackingCreature(gameData, permanentIds);
        } else if (gameData.pendingDestroyRestMode && gameData.pendingForcedSacrificeCount > 0) {
            handleDestroyRestChoice(gameData, permanentIds);
        } else if (gameData.pendingForcedSacrificeCount > 0) {
            handleForcedSacrifice(gameData, permanentIds);
        } else if (gameData.pendingCombatDamageBounceTargetPlayerId != null) {
            handleCombatDamageBounce(gameData, playerId, permanentIds);
        } else if (gameData.pendingAimCounterPlacement) {
            handleAimCounterPlacement(gameData, permanentIds);
        } else if (gameData.pendingOwnPermanentCounterPlacement) {
            handleOwnPermanentCounterPlacement(gameData, permanentIds);
        } else if (gameData.pendingAwakeningCounterPlacement) {
            handleAwakeningCounterPlacement(gameData, playerId, permanentIds);
        } else if (gameData.pendingProliferateCount > 0) {
            handleProliferate(gameData, playerId, permanentIds);
        } else if (gameData.pendingTapSubtypeBoostSourcePermanentId != null) {
            handleTapSubtypeBoost(gameData, playerId, permanentIds);
        } else if (gameData.pendingCapriciousEfreetState != null) {
            handleCapriciousEfreetOpponentTargets(gameData, permanentIds);
        } else if (gameData.pendingPileSeparation) {
            handlePileSeparation(gameData, permanentIds);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    private void handleSacrificeSelfToDestroy(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        UUID sourcePermId = gameData.pendingSacrificeSelfToDestroySourceId;
        gameData.pendingSacrificeSelfToDestroySourceId = null;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
            if (source != null) {
                if (permanentRemovalService.removePermanentToGraveyard(gameData, source)) {
                    triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, source.getCard());
                    String logEntry = source.getCard().getName() + " is sacrificed.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sacrificed for combat damage trigger", gameData.id, source.getCard().getName());

                    UUID chosenPermId = permanentIds.getFirst();
                    Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
                    if (target != null) {
                        if (permanentRemovalService.tryDestroyPermanent(gameData, target)) {
                            String destroyLog = target.getCard().getName() + " is destroyed.";
                            gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                            log.info("Game {} - {} destroyed by sacrifice trigger", gameData.id, target.getCard().getName());
                        }
                    }
                }

                permanentRemovalService.removeOrphanedAuras(gameData);
            } else {
                String logEntry = "Source creature no longer exists — sacrifice trigger fizzles.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleExileDamagedPlayerControlsPermanent(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        gameData.pendingExileDamagedPlayerControlsPermanent = false;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to exile a permanent.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            UUID chosenPermId = permanentIds.getFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, chosenPermId);
            if (target != null) {
                permanentRemovalService.removePermanentToExile(gameData, target);
                String logEntry = target.getCard().getName() + " is exiled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled by combat damage trigger", gameData.id, target.getCard().getName());

                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleSacrificeAttackingCreature(GameData gameData, List<UUID> permanentIds) {
        gameData.pendingSacrificeAttackingCreature = false;

        for (UUID permId : permanentIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, permId);
            if (creature != null) {
                UUID ownerId = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf != null && bf.contains(creature)) {
                        ownerId = pid;
                        break;
                    }
                }
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                String ownerName = ownerId != null ? gameData.playerIdToName.get(ownerId) : "Unknown";
                String logEntry = ownerName + " sacrifices " + creature.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, creature.getCard().getName());
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleForcedSacrifice(GameData gameData, List<UUID> permanentIds) {
        UUID sacrificingPlayerId = gameData.pendingForcedSacrificePlayerId;
        gameData.pendingForcedSacrificeCount = 0;
        gameData.pendingForcedSacrificePlayerId = null;

        boolean simultaneousFlow = !gameData.pendingSimultaneousSacrificeIds.isEmpty()
                || !gameData.pendingForcedSacrificeQueue.isEmpty();

        if (simultaneousFlow) {
            // "Each player sacrifices" flow — defer actual sacrifice until all players have chosen.
            // Per CR 101.4: all chosen permanents are sacrificed at the same time.
            gameData.pendingSimultaneousSacrificeIds.addAll(permanentIds);

            if (!gameData.pendingForcedSacrificeQueue.isEmpty()) {
                // More players still need to choose — prompt the next one
                var next = gameData.pendingForcedSacrificeQueue.removeFirst();
                gameData.pendingForcedSacrificeCount = next.count();
                gameData.pendingForcedSacrificePlayerId = next.playerId();
                playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                        next.count(), "Choose " + next.count() + " permanent"
                                + (next.count() > 1 ? "s" : "") + " to sacrifice.");
                return;
            }

            // All players have chosen — sacrifice all simultaneously
            List<UUID> allIds = new ArrayList<>(gameData.pendingSimultaneousSacrificeIds);
            gameData.pendingSimultaneousSacrificeIds.clear();

            for (UUID permId : allIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                    String ownerName = controllerId != null ? gameData.playerIdToName.get(controllerId) : "Unknown";
                    permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                    String logEntry = ownerName + " sacrifices " + perm.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, perm.getCard().getName());
                }
            }
        } else {
            // Direct forced sacrifice (e.g. Phyrexian Obliterator) — sacrifice immediately
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    String ownerName = sacrificingPlayerId != null ? gameData.playerIdToName.get(sacrificingPlayerId) : "Unknown";
                    permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                    String logEntry = ownerName + " sacrifices " + perm.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sacrifices {}", gameData.id, ownerName, perm.getCard().getName());
                }
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // Follow the same pattern as proliferate completion: SBA → may abilities → resume effects
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleDestroyRestChoice(GameData gameData, List<UUID> permanentIds) {
        destructionResolutionService.completeDestroyRestChoice(gameData, permanentIds);

        // If we're still awaiting input (next player's choice), return
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Destruction is complete — follow standard completion: SBA → may abilities → resume effects
        permanentRemovalService.removeOrphanedAuras(gameData);
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleCombatDamageBounce(GameData gameData, UUID playerId, List<UUID> permanentIds) {
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
                permanentRemovalService.removeOrphanedAuras(gameData);
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
    }

    private void handleAwakeningCounterPlacement(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        gameData.pendingAwakeningCounterPlacement = false;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to put awakening counters on any lands.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            List<String> awakenedNames = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.setAwakeningCounters(perm.getAwakeningCounters() + 1);
                    awakenedNames.add(perm.getCard().getName());
                }
            }

            if (!awakenedNames.isEmpty()) {
                String logEntry = String.join(", ", awakenedNames)
                        + (awakenedNames.size() == 1 ? " receives" : " receive")
                        + " an awakening counter and "
                        + (awakenedNames.size() == 1 ? "becomes an" : "become")
                        + " 8/8 green Elemental creature"
                        + (awakenedNames.size() == 1 ? "." : "s.");
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - Awakening counters placed on {} lands", gameData.id, awakenedNames.size());
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.advanceStep(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleAimCounterPlacement(GameData gameData, List<UUID> permanentIds) {
        gameData.pendingAimCounterPlacement = false;

        if (gameData.pendingEffectResolutionEntry != null) {
            permanentCounterResolutionService.placeCountersOnPermanents(gameData,
                    gameData.pendingEffectResolutionEntry, permanentIds, CounterType.AIM);
        }

        // Resume resolving remaining effects on the same spell/ability
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleOwnPermanentCounterPlacement(GameData gameData, List<UUID> permanentIds) {
        gameData.pendingOwnPermanentCounterPlacement = false;
        CounterType counterType = gameData.pendingOwnPermanentCounterType;
        int count = gameData.pendingOwnPermanentCounterCount;
        gameData.pendingOwnPermanentCounterType = null;
        gameData.pendingOwnPermanentCounterCount = 0;

        if (!permanentIds.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                permanentCounterResolutionService.placeCounterOnPermanent(gameData,
                        gameData.pendingEffectResolutionEntry, target, counterType, count);
            }
        }

        // Resume resolving remaining effects on the same spell/ability
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleProliferate(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        gameData.pendingProliferateCount--;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to proliferate any permanents.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            List<String> proliferatedNames = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    if (!gameQueryService.cantHaveCounters(gameData, perm)) {
                        if (perm.getPlusOnePlusOneCounters() > 0) {
                            perm.setPlusOnePlusOneCounters(perm.getPlusOnePlusOneCounters() + 1);
                        }
                        if (perm.getMinusOneMinusOneCounters() > 0
                                && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, perm)) {
                            perm.setMinusOneMinusOneCounters(perm.getMinusOneMinusOneCounters() + 1);
                        }
                        if (perm.getLoyaltyCounters() > 0) {
                            perm.setLoyaltyCounters(perm.getLoyaltyCounters() + 1);
                        }
                        if (perm.getSlimeCounters() > 0) {
                            perm.setSlimeCounters(perm.getSlimeCounters() + 1);
                        }
                        if (perm.getAwakeningCounters() > 0) {
                            perm.setAwakeningCounters(perm.getAwakeningCounters() + 1);
                        }
                        if (perm.getAimCounters() > 0) {
                            perm.setAimCounters(perm.getAimCounters() + 1);
                        }
                    }
                    proliferatedNames.add(perm.getCard().getName());
                }
            }

            if (!proliferatedNames.isEmpty()) {
                String logEntry = "Proliferate adds counters to " + String.join(", ", proliferatedNames) + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - Proliferated {} permanents", gameData.id, proliferatedNames.size());
            }
        }

        // More proliferates remaining (e.g. "proliferate, then proliferate again")
        // Per MTG Rule 704.3, SBA are not checked during ability resolution,
        // so defer SBA until all proliferates are done.
        if (gameData.pendingProliferateCount > 0) {
            List<UUID> eligiblePermanentIds = new ArrayList<>();
            gameData.forEachPermanent((pid, p) -> {
                if (p.getPlusOnePlusOneCounters() > 0
                        || p.getMinusOneMinusOneCounters() > 0
                        || p.getLoyaltyCounters() > 0
                        || p.getSlimeCounters() > 0
                        || p.getAwakeningCounters() > 0
                        || p.getAimCounters() > 0) {
                    eligiblePermanentIds.add(p.getId());
                }
            });
            if (eligiblePermanentIds.isEmpty()) {
                gameData.pendingProliferateCount = 0;
                String logEntry = "Proliferate: no permanents with counters to choose.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                playerInputService.beginMultiPermanentChoice(gameData, playerId, eligiblePermanentIds,
                        eligiblePermanentIds.size(), "Proliferate: Choose permanents to add counters to.");
                return;
            }
        }

        // All proliferates done — now check SBA
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability (e.g. "Proliferate. Draw a card.")
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
    }

    private void handleTapSubtypeBoost(GameData gameData, UUID playerId, List<UUID> permanentIds) {
        UUID sourcePermanentId = gameData.pendingTapSubtypeBoostSourcePermanentId;
        gameData.pendingTapSubtypeBoostSourcePermanentId = null;

        int count = permanentIds.size();

        if (count == 0) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to tap any Myr.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            // Tap the chosen permanents
            List<String> tappedNames = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.tap();
                    triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                    tappedNames.add(perm.getCard().getName());
                }
            }

            if (!tappedNames.isEmpty()) {
                String tapLog = gameData.playerIdToName.get(playerId) + " taps " + tappedNames.size()
                        + " Myr: " + String.join(", ", tappedNames) + ".";
                gameBroadcastService.logAndBroadcast(gameData, tapLog);
                log.info("Game {} - {} taps {} Myr for attack trigger", gameData.id,
                        gameData.playerIdToName.get(playerId), tappedNames.size());
            }

            // Boost source permanent +X/+0 (only if still on battlefield)
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            String sourceName;
            if (sourcePermanent != null) {
                sourcePermanent.setPowerModifier(sourcePermanent.getPowerModifier() + count);
                sourceName = sourcePermanent.getCard().getName();
                String boostLog = sourceName + " gets +" + count + "/+0 until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, boostLog);
                log.info("Game {} - {} gets +{}/+0", gameData.id, sourceName, count);
            } else {
                sourceName = "Myr Battlesphere";
                log.info("Game {} - Source permanent no longer on battlefield, skipping boost", gameData.id);
            }

            // Deal X damage to the defending player (happens even if source left battlefield per ruling)
            UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, playerId);
            String defenderName = gameData.playerIdToName.get(defendingPlayerId);

            // Check source damage prevention
            Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(defendingPlayerId);
            boolean sourcePrevented = preventedSources != null && preventedSources.contains(sourcePermanentId);

            if (sourcePrevented) {
                gameBroadcastService.logAndBroadcast(gameData, sourceName + "'s damage to " + defenderName + " is prevented.");
            } else {
                // Apply damage multiplier (DoubleDamageEffect)
                int damage = count;
                final int[] multiplier = {1};
                gameData.forEachPermanent((pid, p) -> {
                    for (CardEffect e : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (e instanceof DoubleDamageEffect) {
                            multiplier[0] *= 2;
                        }
                    }
                });
                damage *= multiplier[0];

                // Apply global prevention shield
                if (gameData.globalDamagePreventionShield > 0 && damage > 0) {
                    int prevented = Math.min(gameData.globalDamagePreventionShield, damage);
                    gameData.globalDamagePreventionShield -= prevented;
                    damage -= prevented;
                }

                // Apply player prevention shield
                int shield = gameData.playerDamagePreventionShields.getOrDefault(defendingPlayerId, 0);
                if (shield > 0 && damage > 0) {
                    int prevented = Math.min(shield, damage);
                    gameData.playerDamagePreventionShields.put(defendingPlayerId, shield - prevented);
                    damage -= prevented;
                }

                if (damage > 0) {
                    boolean hasInfect = sourcePermanent != null
                            && gameQueryService.hasKeyword(gameData, sourcePermanent, Keyword.INFECT);
                    boolean treatAsInfect = hasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, defendingPlayerId);
                    if (treatAsInfect && gameQueryService.canPlayerGetPoisonCounters(gameData, defendingPlayerId)) {
                        int currentPoison = gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0);
                        gameData.playerPoisonCounters.put(defendingPlayerId, currentPoison + damage);
                        gameBroadcastService.logAndBroadcast(gameData, defenderName + " gets "
                                + damage + " poison counter" + (damage > 1 ? "s" : "") + " from " + sourceName + ".");
                    } else if (!gameQueryService.canPlayerLifeChange(gameData, defendingPlayerId)) {
                        gameBroadcastService.logAndBroadcast(gameData,
                                defenderName + "'s life total can't change.");
                    } else {
                        int currentLife = gameData.getLife(defendingPlayerId);
                        gameData.playerLifeTotals.put(defendingPlayerId, currentLife - damage);
                        gameBroadcastService.logAndBroadcast(gameData, sourceName + " deals "
                                + damage + " damage to " + defenderName + ".");
                    }
                    gameData.playersDealtDamageThisTurn.add(defendingPlayerId);
                }
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleCapriciousEfreetOpponentTargets(GameData gameData, List<UUID> permanentIds) {
        PendingCapriciousEfreetState state = gameData.pendingCapriciousEfreetState;
        gameData.pendingCapriciousEfreetState = null;

        // Combine own target + opponent targets
        List<UUID> allTargets = new ArrayList<>();
        allTargets.add(state.ownTargetId());
        allTargets.addAll(permanentIds);

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                state.sourceCard(),
                state.controllerId(),
                state.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyOneOfTargetsAtRandomEffect())),
                0,
                allTargets
        );
        gameData.stack.add(entry);

        List<String> targetNames = new ArrayList<>();
        for (UUID targetId : allTargets) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            targetNames.add(target != null ? target.getCard().getName() : targetId.toString());
        }
        String logEntry = state.sourceCard().getName() + "'s ability targets " + String.join(", ", targetNames) + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} upkeep trigger targets: {}", gameData.id, state.sourceCard().getName(), targetNames);

        // Continue processing: more Efreet triggers → may abilities → priority
        if (!gameData.pendingCapriciousEfreetTargets.isEmpty()) {
            turnProgressionService.processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handlePileSeparation(GameData gameData, List<UUID> permanentIds) {
        destructionResolutionService.completePileSeparationStep1(gameData, permanentIds);
    }
}
