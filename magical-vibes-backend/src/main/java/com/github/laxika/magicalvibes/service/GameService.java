package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.ExileResolutionService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.XValueChoiceHandlerService;
import com.github.laxika.magicalvibes.service.battlefield.ExileResolutionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRegistry gameRegistry;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;
    private final ChoiceHandlerService listChoiceHandlerService;
    private final CardChoiceHandlerService cardChoiceHandlerService;
    private final PermanentChoiceHandlerService permanentChoiceHandlerService;
    private final GraveyardChoiceHandlerService graveyardChoiceHandlerService;
    private final MayAbilityHandlerService mayAbilityHandlerService;
    private final XValueChoiceHandlerService xValueChoiceHandlerService;
    private final LibraryChoiceHandlerService libraryChoiceHandlerService;
    private final SpellCastingService spellCastingService;
    private final StackResolutionService stackResolutionService;
    private final AbilityActivationService abilityActivationService;
    private final MulliganService mulliganService;
    private final ReconnectionService reconnectionService;
    private final ExileResolutionService exileResolutionService;
    private final GameOutcomeService gameOutcomeService;

    /**
     * Validates that the game is running, no interaction is awaiting input, and the given player
     * currently holds priority. Must be called inside the {@code synchronized(gameData)} block
     * after {@link #resolveActingPlayer(GameData, Player)}.
     *
     * @throws IllegalStateException if the game is not running, input is awaited, or the player
     *                               does not have priority
     */
    private void requirePriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        if (gameData.interaction.isAwaitingInput()) {
            throw new IllegalStateException("Cannot perform this action while awaiting input");
        }
        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (priorityHolder == null || !priorityHolder.equals(player.getId())) {
            throw new IllegalStateException("You do not have priority");
        }
    }

    /**
     * the controlled player when the controlled player should be acting (has priority
     * or is the expected respondent for an interaction).
     * If the controller is acting as themselves (e.g., passing their own priority as
     * non-active player), the original player is returned.
     */
    private Player resolveActingPlayer(GameData gameData, Player player) {
        if (gameData.mindControllerPlayerId == null) return player;
        if (!player.getId().equals(gameData.mindControllerPlayerId)) return player;
        UUID controlledId = gameData.mindControlledPlayerId;
        if (controlledId == null) return player;

        // Check if the controlled player is the expected respondent for an interaction
        if (gameData.interaction.isAwaitingInput()) {
            InteractionContext ctx = gameData.interaction.currentContext();
            if (ctx != null && controlledPlayerMatchesContext(ctx, controlledId)) {
                return new Player(controlledId, gameData.playerIdToName.get(controlledId));
            }
        }

        // Check if the controlled player currently holds priority
        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (controlledId.equals(priorityHolder)) {
            return new Player(controlledId, gameData.playerIdToName.get(controlledId));
        }

        return player; // Controller acts as themselves
    }

    private boolean controlledPlayerMatchesContext(InteractionContext ctx, UUID controlledId) {
        return switch (ctx) {
            case InteractionContext.AttackerDeclaration ad -> controlledId.equals(ad.activePlayerId());
            case InteractionContext.BlockerDeclaration bd -> controlledId.equals(bd.defenderId());
            case InteractionContext.CardChoice cc -> controlledId.equals(cc.playerId());
            case InteractionContext.PermanentChoice pc -> controlledId.equals(pc.playerId());
            case InteractionContext.GraveyardChoice gc -> controlledId.equals(gc.playerId());
            case InteractionContext.ColorChoice cc -> controlledId.equals(cc.playerId());
            case InteractionContext.MayAbilityChoice mc -> controlledId.equals(mc.playerId());
            case InteractionContext.MultiPermanentChoice mpc -> controlledId.equals(mpc.playerId());
            case InteractionContext.MultiGraveyardChoice mgc -> controlledId.equals(mgc.playerId());
            case InteractionContext.LibraryReorder lr -> controlledId.equals(lr.playerId());
            case InteractionContext.LibrarySearch ls -> controlledId.equals(ls.playerId());
            case InteractionContext.LibraryRevealChoice lrc -> controlledId.equals(lrc.playerId());
            case InteractionContext.HandTopBottomChoice htbc -> controlledId.equals(htbc.playerId());
            case InteractionContext.RevealedHandChoice rhc -> controlledId.equals(rhc.choosingPlayerId());
            case InteractionContext.CombatDamageAssignment cda -> controlledId.equals(cda.playerId());
            case InteractionContext.MultiZoneExileChoice mzec -> controlledId.equals(mzec.playerId());
            case InteractionContext.XValueChoice xvc -> controlledId.equals(xvc.playerId());
            case InteractionContext.Scry sc -> controlledId.equals(sc.playerId());
            case InteractionContext.KnowledgePoolCastChoice kpc -> controlledId.equals(kpc.playerId());
            case InteractionContext.MirrorOfFateChoice mfc -> controlledId.equals(mfc.playerId());
        };
    }

    public void passPriority(GameData gameData, Player player) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);

            gameData.priorityPassedBy.add(player.getId());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameData.id, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                if (!gameData.stack.isEmpty()) {
                    stackResolutionService.resolveTopOfStack(gameData);
                } else {
                    turnProgressionService.advanceStep(gameData);
                }
            } else {
                gameBroadcastService.broadcastGameState(gameData);
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void paySearchTax(GameData gameData, Player player) {
        synchronized (gameData) {
            requirePriority(gameData, player);

            // Find unpaid CantSearchLibrariesEffect permanents
            List<UUID> unpaidArbiterIds = new java.util.ArrayList<>();
            gameData.forEachPermanent((playerId, permanent) -> {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantSearchLibrariesEffect) {
                        Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(player.getId());
                        if (paidSet == null || !paidSet.contains(permanent.getId())) {
                            unpaidArbiterIds.add(permanent.getId());
                        }
                    }
                }
            });

            if (unpaidArbiterIds.isEmpty()) {
                throw new IllegalStateException("No unpaid search tax to pay");
            }

            int totalCost = unpaidArbiterIds.size() * 2;
            ManaCost cost = new ManaCost("{" + totalCost + "}");
            ManaPool pool = gameData.playerManaPools.get(player.getId());

            if (pool == null || !cost.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay search tax");
            }

            cost.pay(pool);

            Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds
                    .computeIfAbsent(player.getId(), k -> ConcurrentHashMap.newKeySet());
            paidSet.addAll(unpaidArbiterIds);

            String logEntry = player.getUsername() + " pays {" + totalCost + "} for Leonin Arbiter search tax.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} pays {{}} for Leonin Arbiter search tax (special action)",
                    gameData.id, player.getUsername(), totalCost);

            gameBroadcastService.broadcastGameState(gameData);
        }
    }

    public void surrender(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status == GameStatus.FINISHED) {
                throw new IllegalStateException("Game is already finished");
            }
            UUID opponentId = gameQueryService.getOpponentId(gameData, player.getId());
            String logEntry = player.getUsername() + " surrenders!";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            gameOutcomeService.declareWinner(gameData, opponentId);
        }
    }

    public void advanceStep(GameData gameData) {
        turnProgressionService.advanceStep(gameData);
    }

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        synchronized (gameData) {
            reconnectionService.resendAwaitingInput(gameData, playerId);
        }
    }

    public void keepHand(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.keepHand(gameData, player);
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.bottomCards(gameData, player, cardIndices);
        }
    }

    public void mulligan(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.mulligan(gameData, player);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, List.of(), List.of(), false, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, false, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetPermanentId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetPermanentId, List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetPermanentId, List<UUID> targetPermanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetPermanentId, targetPermanentIds);
        }
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetPermanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromExile(gameData, player, exileCardId, xValue, targetPermanentId);
        }
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.tapPermanent(gameData, player, permanentIndex);
        }
    }

    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetPermanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.sacrificePermanent(gameData, player, permanentIndex, targetPermanentId);
        }
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone) {
        activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone, List<UUID> targetPermanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone, targetPermanentIds);
        }
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex);
        }
    }

    public void setAutoStops(GameData gameData, Player player, List<TurnStep> stops) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Set<TurnStep> stopSet = ConcurrentHashMap.newKeySet();
            stopSet.addAll(stops);
            stopSet.add(TurnStep.PRECOMBAT_MAIN);
            stopSet.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(player.getId(), stopSet);
            gameBroadcastService.broadcastGameState(gameData);
        }
    }

    // ===== Delegated user input handlers =====

    public void handleListChoice(GameData gameData, Player player, String choiceName) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            listChoiceHandlerService.handleListChoice(gameData, player, choiceName);
        }
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            cardChoiceHandlerService.handleCardChosen(gameData, player, cardIndex);
        }
    }

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            permanentChoiceHandlerService.handlePermanentChosen(gameData, player, permanentId);
        }
    }

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (gameData.interaction.awaitingInputType() == AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE) {
                abilityActivationService.handleActivatedAbilityGraveyardExileCostChosen(gameData, player, cardIndex);
            } else {
                graveyardChoiceHandlerService.handleGraveyardCardChosen(gameData, player, cardIndex);
            }
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            permanentChoiceHandlerService.handleMultiplePermanentsChosen(gameData, player, permanentIds);
        }
    }

    public void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (gameData.interaction.awaitingInputType() == AwaitingInput.LIBRARY_REVEAL_CHOICE) {
                libraryChoiceHandlerService.handleLibraryRevealChoice(gameData, player, cardIds);
            } else if (gameData.interaction.awaitingInputType() == AwaitingInput.MULTI_ZONE_EXILE_CHOICE) {
                listChoiceHandlerService.handleMultiZoneExileCardsChosen(gameData, player, cardIds);
            } else if (gameData.interaction.awaitingInputType() == AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE) {
                exileResolutionService.handleKnowledgePoolCastChoice(gameData, player, cardIds);
            } else if (gameData.interaction.awaitingInputType() == AwaitingInput.MIRROR_OF_FATE_CHOICE) {
                exileResolutionService.handleMirrorOfFateChoice(gameData, player, cardIds);
            } else {
                graveyardChoiceHandlerService.handleMultipleGraveyardCardsChosen(gameData, player, cardIds);
            }
        }
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            mayAbilityHandlerService.handleMayAbilityChosen(gameData, player, accepted);
        }
    }

    public void handleXValueChosen(GameData gameData, Player player, int chosenValue) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            xValueChoiceHandlerService.handleXValueChosen(gameData, player, chosenValue);
        }
    }

    public void handleScryCompleted(GameData gameData, Player player, List<Integer> topCardOrder, List<Integer> bottomCardOrder) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            libraryChoiceHandlerService.handleScryCompleted(gameData, player, topCardOrder, bottomCardOrder);
        }
    }

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            libraryChoiceHandlerService.handleLibraryCardsReordered(gameData, player, cardOrder);
        }
    }

    public void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            libraryChoiceHandlerService.handleHandTopBottomChosen(gameData, player, handCardIndex, topCardIndex);
        }
    }

    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            libraryChoiceHandlerService.handleLibraryCardChosen(gameData, player, cardIndex);
        }
    }

    // ===== Combat wrapper methods =====

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        declareAttackers(gameData, player, attackerIndices, null);
    }

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            turnProgressionService.handleCombatResult(combatService.declareAttackers(gameData, player, attackerIndices, attackTargets), gameData);
        }
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            turnProgressionService.handleCombatResult(combatService.declareBlockers(gameData, player, blockerAssignments), gameData);
        }
    }

    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
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

}



