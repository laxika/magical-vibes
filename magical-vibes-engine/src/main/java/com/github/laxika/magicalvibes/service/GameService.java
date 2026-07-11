package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.interaction.CombatDamageAssignmentInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
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
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SpellCastingService spellCastingService;
    private final StackResolutionService stackResolutionService;
    private final AbilityActivationService abilityActivationService;
    private final MulliganService mulliganService;
    private final ReconnectionService reconnectionService;
    private final ExileSupport exileSupport;
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
     * Returns true if the game is currently in attacker declaration and the given player
     * is the declaring player. Per CR 508.1i, mana abilities may be activated during this window.
     */
    private boolean isAttackTaxManaPayment(GameData gameData, Player player) {
        return gameData.interaction.activeInteraction() instanceof PendingInteraction.AttackerDeclaration ad
                && ad.activePlayerId().equals(player.getId());
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
            UUID activeDecider = interactionHandlerRegistry.activeDecidingPlayerId(gameData);
            if (controlledId.equals(activeDecider)) {
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

    public void passPriority(GameData gameData, Player player) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);

            // Passing priority closes the window in which mana-ability activations could
            // still be undone by the cancel-casting UI.
            gameData.revertableManaActivations.clear();

            // CR 603.3: Flush triggers deferred from mana-ability activations.
            // They go on the stack now (the next time a player would receive priority)
            // and both players must pass again before the top resolves.
            if (!gameData.pendingManaAbilityTriggers.isEmpty()) {
                gameData.stack.addAll(gameData.pendingManaAbilityTriggers);
                gameData.pendingManaAbilityTriggers.clear();
                gameData.priorityPassedBy.clear();
            }

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

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, List.of(), List.of(), false, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, false, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, false);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, List.of(), null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, exileGraveyardCardIndices, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                    exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, UUID graveyardCardId, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardId, xValue, targetId, targetIds, exileGraveyardCardIndices, chosenGraveyardType);
        }
    }

    public void playCardWithEvoke(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithEvoke(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of());
        }
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromExile(gameData, player, exileCardId, xValue, targetId);
        }
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromLibraryTop(gameData, player, xValue, targetId);
        }
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!isAttackTaxManaPayment(gameData, player)) {
                requirePriority(gameData, player);
            }
            abilityActivationService.tapPermanent(gameData, player, permanentIndex);
        }
    }

    /**
     * Undoes the player's still-revertable mana-ability activations (MTGO-style cancel while
     * paying for a spell): tapped sources untap and the mana they produced leaves the pool.
     * Allowed whenever the player could have activated the abilities in the first place —
     * holding priority, or while paying attack tax during attacker declaration.
     */
    public void revertManaActivations(GameData gameData, Player player) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!isAttackTaxManaPayment(gameData, player)) {
                requirePriority(gameData, player);
            }
            abilityActivationService.revertManaActivations(gameData, player);
        }
    }

    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.sacrificePermanent(gameData, player, permanentIndex, targetId);
        }
    }

    public void tapForeignLandForMana(GameData gameData, Player player, UUID permanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.tapForeignLandForMana(gameData, player, permanentId);
        }
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone) {
        activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds) {
        activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (isAttackTaxManaPayment(gameData, player)) {
                // CR 508.1i: only mana abilities allowed during attacker declaration
                if (!abilityActivationService.isManaAbilityAt(gameData, player.getId(), permanentIndex, abilityIndex)) {
                    throw new IllegalStateException("Only mana abilities can be activated during attacker declaration");
                }
            } else {
                requirePriority(gameData, player);
            }
            abilityActivationService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments);
        }
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex);
        }
    }

    /**
     * Pure legality query: could {@code playerId} activate the ability at {@code abilityIndex} on
     * {@code permanent} right now? Runs the engine's own activation checks (everything except
     * target choice, with X assumed 0) against the given mana pool, which may be hypothetical.
     * Never mutates game state. Exposed so AI players share the engine's legality rules instead
     * of re-implementing them.
     */
    public boolean canActivateAbility(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, ManaPool manaPool) {
        synchronized (gameData) {
            return abilityActivationService.canActivateAbility(gameData, playerId, permanent, abilityIndex, manaPool);
        }
    }

    /**
     * Returns the activated abilities currently available on a permanent (own + static-granted +
     * temporary), in {@code abilityIndex} order. Read-only.
     */
    public List<ActivatedAbility> getEffectiveActivatedAbilities(GameData gameData, Permanent permanent) {
        synchronized (gameData) {
            return abilityActivationService.getEffectiveActivatedAbilities(gameData, permanent);
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


    public void handleListChoice(GameData gameData, Player player, String choiceName) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.ListChoiceMade(choiceName))) {
                throw new IllegalStateException("Not awaiting color choice");
            }
        }
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.CardIndexChosen(cardIndex))) {
                throw new IllegalStateException("Not awaiting card choice");
            }
        }
    }

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.PermanentChosen(permanentId))) {
                throw new IllegalStateException("Not awaiting permanent choice");
            }
        }
    }

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.GraveyardCardChosen(cardIndex))) {
                throw new IllegalStateException("Not awaiting graveyard choice");
            }
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.PermanentsChosen(permanentIds))) {
                throw new IllegalStateException("Not awaiting multi-permanent choice");
            }
        }
    }

    public void handleMultipleCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.CardsChosen(cardIds))) {
                return;
            }
            throw new IllegalStateException("Not awaiting multi-graveyard choice");
        }
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.MayAbilityChosen(accepted))) {
                throw new IllegalStateException("Not awaiting may ability choice");
            }
        }
    }

    public void handleXValueChosen(GameData gameData, Player player, int chosenValue) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.NumberChosen(chosenValue))) {
                throw new IllegalStateException("Not awaiting X value choice");
            }
        }
    }

    public void handleScryCompleted(GameData gameData, Player player, List<Integer> topCardOrder, List<Integer> bottomCardOrder) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.ScryOrder(topCardOrder, bottomCardOrder))) {
                throw new IllegalStateException("Not awaiting scry");
            }
        }
    }

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.CardOrder(cardOrder))) {
                throw new IllegalStateException("Not awaiting library reorder");
            }
        }
    }

    public void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.HandTopBottom(handCardIndex, topCardIndex))) {
                throw new IllegalStateException("Not awaiting hand/top/bottom choice");
            }
        }
    }

    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.LibraryCardChosen(cardIndex))) {
                throw new IllegalStateException("Not awaiting library search");
            }
        }
    }


    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        declareAttackers(gameData, player, attackerIndices, null);
    }

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.AttackersDeclared(attackerIndices, attackTargets))) {
                return;
            }
            // No declaration is active — preserve the legacy stray-message path (the combat
            // flow rejects with "Not awaiting attacker declaration" and re-sends).
            try {
                turnProgressionService.handleCombatResult(combatService.declareAttackers(gameData, player, attackerIndices, attackTargets), gameData);
            } catch (IllegalStateException | IllegalArgumentException e) {
                // Re-send available attackers so the player (or AI) can retry
                combatService.handleDeclareAttackersStep(gameData);
                throw e;
            }
        }
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.BlockersDeclared(blockerAssignments))) {
                return;
            }
            // No declaration is active — preserve the legacy stray-message path (the combat
            // flow rejects with "Not awaiting blocker declaration").
            turnProgressionService.handleCombatResult(combatService.declareBlockers(gameData, player, blockerAssignments), gameData);
        }
    }

    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.CombatDamageAssigned(attackerIndex, assignments))) {
                // No assignment prompt is active — preserve the legacy stray-message path
                // (the combat flow itself rejects with "Not in combat damage assignment
                // phase" and re-sends; the legacy entry never consulted the interaction).
                CombatDamageAssignmentInteractionHandler.applyAssignment(gameData, player,
                        attackerIndex, assignments, combatService, turnProgressionService);
            }
        }
    }

}



