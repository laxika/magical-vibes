package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingCapriciousEfreetState;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
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
    private final DestructionSupport destructionSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AnimationSupport animationSupport;
    private final LifeSupport lifeSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport librarySearchSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class) == null) {
            throw new IllegalStateException("Not awaiting multi-permanent choice");
        }
        PendingInteraction.MultiPermanentChoice multiPermanentChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        if (multiPermanentChoice == null || !player.getId().equals(multiPermanentChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<UUID> validIds = multiPermanentChoice.validIds();
        int maxCount = multiPermanentChoice.maxCount();

        gameData.interaction.clearAwaitingInput();

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

        MultiPermanentChoiceContext context = multiPermanentChoice.context();
        if (context instanceof MultiPermanentChoiceContext.ExileDamagedPlayerControls) {
            handleExileDamagedPlayerControlsPermanent(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyDamagedPlayerControls ctx) {
            handleDestroyDamagedPlayerControlsPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeDamagedPlayerControls ctx) {
            handleSacrificeDamagedPlayerControlsPermanent(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeSelfToDestroy ctx) {
            handleSacrificeSelfToDestroy(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TransformAndAttach ctx) {
            handleTransformAndAttach(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeAttackingCreatures) {
            handleSacrificeAttackingCreature(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.CombatDamageBounce ctx) {
            handleCombatDamageBounce(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AimCounterPlacement) {
            handleAimCounterPlacement(gameData, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.OwnPermanentCounterPlacement ctx) {
            handleOwnPermanentCounterPlacement(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.AwakeningCounterPlacement) {
            handleAwakeningCounterPlacement(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.Proliferate ctx) {
            handleProliferate(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapSubtypeBoost ctx) {
            handleTapSubtypeBoost(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.DestroyRestChoice ctx) {
            handleDestroyRestChoice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedSacrifice ctx) {
            handleForcedSacrifice(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ForcedDestroy ctx) {
            handleForcedDestroy(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.ChooseCreatureRestCantBlock ctx) {
            handleChooseCreatureRestCantBlock(gameData, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.TapCreaturesGainLife ctx) {
            handleTapCreaturesGainLife(gameData, playerId, permanentIds, ctx);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificeLandsSearchLandsToBattlefieldTapped) {
            handleSacrificeLandsSearchLandsToBattlefieldTapped(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.SacrificePermanentsDrawPerSacrificed) {
            handleSacrificePermanentsDrawPerSacrificed(gameData, playerId, permanentIds);
        } else if (context instanceof MultiPermanentChoiceContext.StaticOrbUntap ctx) {
            handleStaticOrbUntap(gameData, permanentIds, ctx);
        } else if (gameData.hasPendingInteraction(PendingCapriciousEfreetState.class)) {
            handleCapriciousEfreetOpponentTargets(gameData, permanentIds);
        } else if (gameData.hasPendingInteraction(PendingPileSeparation.class)) {
            handlePileSeparation(gameData, permanentIds);
        } else {
            throw new IllegalStateException("No pending multi-permanent choice context");
        }
    }

    private void handleSacrificeSelfToDestroy(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                              MultiPermanentChoiceContext.SacrificeSelfToDestroy context) {
        UUID sourcePermId = context.sourcePermanentId();

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

    private void handleTransformAndAttach(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.TransformAndAttach context) {
        UUID sourcePermId = context.sourcePermanentId();

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to attach.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            animationSupport.completeTransformAndAttach(
                    gameData, playerId, sourcePermId, permanentIds.getFirst());
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleExileDamagedPlayerControlsPermanent(GameData gameData, UUID playerId, List<UUID> permanentIds) {
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

    private void handleDestroyDamagedPlayerControlsPermanent(GameData gameData, List<UUID> permanentIds,
                                                             MultiPermanentChoiceContext.DestroyDamagedPlayerControls context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                destructionSupport.tryDestroyAndLog(gameData, target, context.sourceName());
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleSacrificeDamagedPlayerControlsPermanent(GameData gameData, List<UUID> permanentIds,
                                                               MultiPermanentChoiceContext.SacrificeDamagedPlayerControls context) {
        if (!permanentIds.isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                String ownerName = controllerId != null ? gameData.playerIdToName.get(controllerId) : "Unknown";
                if (permanentRemovalService.removePermanentToGraveyard(gameData, target)) {
                    if (controllerId != null) {
                        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, controllerId, target.getCard());
                    }
                    String logEntry = ownerName + " sacrifices " + target.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sacrificed by {}", gameData.id, target.getCard().getName(), context.sourceName());
                }
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleSacrificeAttackingCreature(GameData gameData, List<UUID> permanentIds) {
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

    private void handleForcedSacrifice(GameData gameData, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.ForcedSacrifice context) {
        UUID sacrificingPlayerId = context.sacrificingPlayerId();

        boolean simultaneousFlow = !context.accumulatedSacrificeIds().isEmpty()
                || !context.remainingChoosers().isEmpty();

        if (simultaneousFlow) {
            // "Each player sacrifices" flow — defer actual sacrifice until all players have chosen.
            // Per CR 101.4: all chosen permanents are sacrificed at the same time.
            List<UUID> allIds = new ArrayList<>(context.accumulatedSacrificeIds());
            allIds.addAll(permanentIds);

            if (!context.remainingChoosers().isEmpty()) {
                // More players still need to choose — prompt the next one
                destructionSupport.beginNextForcedSacrificeFromQueue(gameData,
                        context.remainingChoosers(), allIds);
                return;
            }

            // All players have chosen — sacrifice all simultaneously
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

    private void handleForcedDestroy(GameData gameData, List<UUID> permanentIds,
                                     MultiPermanentChoiceContext.ForcedDestroy context) {
        // Chosen permanents are destroyed simultaneously (regeneration/indestructible apply).
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.tryDestroyAndLog(gameData, perm, context.sourceName());
            }
        }

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

    private void handleSacrificeLandsSearchLandsToBattlefieldTapped(GameData gameData, UUID playerId,
                                                                    List<UUID> permanentIds) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Search the library for up to that many land cards, put them onto the battlefield tapped.
        if (sacrificed > 0 && !librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck == null || deck.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " searches their library but it is empty. Library is shuffled.");
            } else {
                List<Card> lands = deck.stream()
                        .filter(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                        .toList();
                if (lands.isEmpty()) {
                    com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " searches their library but finds no land cards. Library is shuffled.");
                } else {
                    String prompt = "Search your library for up to " + sacrificed + " land card"
                            + (sacrificed != 1 ? "s" : "")
                            + " and put them onto the battlefield tapped (" + sacrificed + " remaining).";
                    librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId,
                            com.github.laxika.magicalvibes.model.LibrarySearchParams.builder(playerId, new ArrayList<>(lands))
                                    .remainingCount(sacrificed)
                                    .canFailToFind(true)
                                    .destination(com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED)
                                    .build(), prompt, true);
                    // Library search interaction is now active; it resumes effect resolution on completion.
                    return;
                }
            }
        }

        // No search begun — follow standard completion: SBA → may abilities → resume effects
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

    private void handleSacrificePermanentsDrawPerSacrificed(GameData gameData, UUID playerId,
                                                            List<UUID> permanentIds) {
        int sacrificed = 0;
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                destructionSupport.sacrificeAndLog(gameData, perm, playerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (sacrificed > 0) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, sacrificed);
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + " sacrifices no permanents.");
        }

        // Standard completion: SBA → may abilities → resume effects
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleDestroyRestChoice(GameData gameData, List<UUID> permanentIds,
                                         MultiPermanentChoiceContext.DestroyRestChoice context) {
        destructionSupport.completeDestroyRestChoice(gameData, permanentIds, context);

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

    private void handleChooseCreatureRestCantBlock(GameData gameData, List<UUID> permanentIds,
                                                   MultiPermanentChoiceContext.ChooseCreatureRestCantBlock context) {
        UUID targetPlayerId = context.targetPlayerId();
        UUID keptId = permanentIds.isEmpty() ? null : permanentIds.getFirst();

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !perm.getId().equals(keptId)) {
                    perm.setCantBlockThisTurn(true);
                    count++;
                }
            }
        }

        if (count > 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    "Other creatures controlled by " + playerName + " can't block this turn.");
        }

        // Standard completion: SBA → may abilities → resume effects
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleCombatDamageBounce(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.CombatDamageBounce context) {
        UUID targetPlayerId = context.targetPlayerId();

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
        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to put awakening counters on any lands.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            List<String> awakenedNames = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    perm.setCounterCount(CounterType.AWAKENING, perm.getCounterCount(CounterType.AWAKENING) + 1);
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
        if (gameData.pendingEffectResolutionEntry != null) {
            permanentCounterSupport.placeCountersOnPermanents(gameData,
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

    private void handleOwnPermanentCounterPlacement(GameData gameData, List<UUID> permanentIds,
                                                    MultiPermanentChoiceContext.OwnPermanentCounterPlacement context) {
        CounterType counterType = context.counterType();
        int count = context.count();

        if (!permanentIds.isEmpty() && gameData.pendingEffectResolutionEntry != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            if (target != null) {
                permanentCounterSupport.placeCounterOnPermanent(gameData,
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

    private void handleProliferate(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                   MultiPermanentChoiceContext.Proliferate context) {
        int remainingProliferates = context.remainingCount() - 1;

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to proliferate any permanents.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            List<String> proliferatedNames = new ArrayList<>();
            for (UUID permId : permanentIds) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    if (!gameQueryService.cantHaveCounters(gameData, perm)) {
                        if (perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
                            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + 1);
                        }
                        if (perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                                && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, perm)) {
                            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + 1);
                            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, perm, 1);
                        }
                        if (perm.getCounterCount(CounterType.LOYALTY) > 0) {
                            perm.setCounterCount(CounterType.LOYALTY, perm.getCounterCount(CounterType.LOYALTY) + 1);
                        }
                        if (perm.getCounterCount(CounterType.SLIME) > 0) {
                            perm.setCounterCount(CounterType.SLIME, perm.getCounterCount(CounterType.SLIME) + 1);
                        }
                        if (perm.getCounterCount(CounterType.AWAKENING) > 0) {
                            perm.setCounterCount(CounterType.AWAKENING, perm.getCounterCount(CounterType.AWAKENING) + 1);
                        }
                        if (perm.getCounterCount(CounterType.AIM) > 0) {
                            perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + 1);
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
        if (remainingProliferates > 0) {
            List<UUID> eligiblePermanentIds = new ArrayList<>();
            gameData.forEachPermanent((pid, p) -> {
                if (p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                        || p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                        || p.getCounterCount(CounterType.LOYALTY) > 0
                        || p.getCounterCount(CounterType.SLIME) > 0
                        || p.getCounterCount(CounterType.AWAKENING) > 0
                        || p.getCounterCount(CounterType.AIM) > 0) {
                    eligiblePermanentIds.add(p.getId());
                }
            });
            if (eligiblePermanentIds.isEmpty()) {
                String logEntry = "Proliferate: no permanents with counters to choose.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                playerInputService.beginMultiPermanentChoice(gameData, playerId, eligiblePermanentIds,
                        eligiblePermanentIds.size(),
                        new MultiPermanentChoiceContext.Proliferate(remainingProliferates),
                        "Proliferate: Choose permanents to add counters to.");
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

    private void handleTapSubtypeBoost(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                       MultiPermanentChoiceContext.TapSubtypeBoost context) {
        UUID sourcePermanentId = context.sourcePermanentId();

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
                // Apply damage multiplier (GlobalDamageMultiplyingEffect)
                int damage = count;
                final int[] multiplier = {1};
                gameData.forEachPermanent((pid, p) -> {
                    for (CardEffect e : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (e instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                            multiplier[0] *= multiplyingEffect.damageMultiplierFactor();
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
                    gameData.recordDamageToPlayer(defendingPlayerId, damage);
                }
            }
        }

        inputCompletionService.sbaMayAbilitiesThenBroadcastAutoPass(gameData);
    }

    private void handleTapCreaturesGainLife(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                            MultiPermanentChoiceContext.TapCreaturesGainLife context) {
        List<String> tappedNames = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !perm.isTapped()) {
                perm.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
                tappedNames.add(perm.getCard().getName());
            }
        }

        int tappedCount = tappedNames.size();
        if (tappedCount == 0) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + " taps no creatures.");
        } else {
            gameBroadcastService.logAndBroadcast(gameData, gameData.playerIdToName.get(playerId)
                    + " taps " + tappedCount + " creature" + (tappedCount == 1 ? "" : "s")
                    + ": " + String.join(", ", tappedNames) + ".");
            lifeSupport.applyGainLife(gameData, playerId, context.lifePerCreature() * tappedCount);
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

    private void handleStaticOrbUntap(GameData gameData, List<UUID> permanentIds,
                                      MultiPermanentChoiceContext.StaticOrbUntap context) {
        UUID activePlayerId = context.activePlayerId();

        List<String> untappedNames = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                untappedNames.add(perm.getCard().getName());
            }
        }
        String playerName = gameData.playerIdToName.get(activePlayerId);
        if (untappedNames.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, playerName + " untaps no permanents (Static Orb).");
        } else {
            gameBroadcastService.logAndBroadcast(gameData, playerName + " untaps "
                    + String.join(", ", untappedNames) + " (Static Orb).");
        }

        // Resume the untap step: only the chosen permanents untap; the rest of the untap-step
        // bookkeeping and turn advance proceed as normal.
        turnProgressionService.resumeStaticOrbUntap(gameData, activePlayerId, new HashSet<>(permanentIds));
    }

    private void handleCapriciousEfreetOpponentTargets(GameData gameData, List<UUID> permanentIds) {
        PendingCapriciousEfreetState state = gameData.pollPendingInteraction(PendingCapriciousEfreetState.class);

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
        if (gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            turnProgressionService.processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            turnProgressionService.processNextPucasMischiefTarget(gameData);
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
        destructionSupport.completePileSeparationStep1(gameData, permanentIds);
    }
}
