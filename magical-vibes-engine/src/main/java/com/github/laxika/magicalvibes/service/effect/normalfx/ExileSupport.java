package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared exile helpers used by every "normal" Exile effect handler and by input dispatch
 * (Knowledge Pool cast choice, Mirror of Fate choice).
 *
 * <p>Extracted verbatim from {@code ExileResolutionService}; behavior is identical.
 */
@Slf4j
@Component
public class ExileSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.event.GameMutationCoordinator mutationCoordinator;
    private final InputCompletionService inputCompletionService;

    // @Lazy mirrors ExileFreeCastSupport: breaks the cycle back through the input services.
    public ExileSupport(GameQueryService gameQueryService,
                        PredicateEvaluationService predicateEvaluationService,
                        GameLogService gameLogService,
                        PermanentRemovalService permanentRemovalService,
                        PlayerInputService playerInputService,
                        TriggerCollectionService triggerCollectionService,
                        com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry,
                        com.github.laxika.magicalvibes.service.event.GameMutationCoordinator mutationCoordinator,
                        @Lazy InputCompletionService inputCompletionService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.permanentRemovalService = permanentRemovalService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.mutationCoordinator = mutationCoordinator;
        this.inputCompletionService = inputCompletionService;
    }

    /**
     * Exiles {@code permanent} outright (no return), logs it against {@code sourceCardName}, and
     * cleans up any orphaned Auras. Shared by the edict-style exile paths (Doomfall).
     */
    public void exilePermanentAndLog(GameData gameData, Permanent permanent, String sourceCardName) {
        Card card = permanent.getCard();
        permanentRemovalService.removePermanentToExile(gameData, permanent);
        gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
        log.info("Game {} - {} exiles {}", gameData.id, sourceCardName, card.getName());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /** Exiles a permanent and keeps the card associated with the source permanent. */
    public void exilePermanentAndTrackWithSource(GameData gameData, Permanent permanent,
                                                 UUID sourcePermanentId, Card sourceCard) {
        Card exiledCard = permanent.getOriginalCard();
        UUID fallbackOwnerId = gameData.findControllerOf(permanent);
        permanentRemovalService.removePermanentToExile(gameData, permanent);

        var exiledEntry = gameData.findExiledCard(exiledCard.getId());
        UUID ownerId = exiledEntry != null ? exiledEntry.ownerId() : fallbackOwnerId;
        gameData.removeFromExile(exiledCard.getId());
        gameData.addToExile(ownerId, exiledCard, sourcePermanentId);

        gameLogService.append(gameData, GameLog.cardTextCard(exiledCard, " is exiled by ", sourceCard, "."));
        log.info("Game {} - {} exiles {} (tracked with source)",
                gameData.id, sourceCard.getName(), exiledCard.getName());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    public void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId, boolean returnTapped) {
        exileAndScheduleReturn(gameData, entry, permanent, ownerId, returnTapped, TurnStep.END_STEP);
    }

    public void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId, boolean returnTapped,
                                        TurnStep returnStep) {
        exileAndScheduleReturn(gameData, entry, permanent, ownerId, returnTapped, returnStep, 0);
    }

    public void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId, boolean returnTapped,
                                        TurnStep returnStep, int plusOnePlusOneCounters) {
        List<Card> cards = permanent.cardsLeavingBattlefield();
        Card card = cards.getFirst();
        permanentRemovalService.removePermanentToExile(gameData, permanent);

        gameLogService.append(gameData, GameLog.cardThen(card, " is exiled. It will return at the beginning of the next "
                + returnStep.getDisplayName().toLowerCase() + "."));
        log.info("Game {} - {} exiles {}; will return at next {}",
                gameData.id, entry.getCard().getName(), card.getName(), returnStep);

        gameData.queueDelayedAction(new PendingExileReturn(
                card, ownerId, returnTapped, false, returnStep, plusOnePlusOneCounters,
                cards.size() == 1 ? List.of() : cards.subList(1, cards.size())));

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Grants {@code ownerId} permission to play (cast) the exiled card until the end of their next
     * turn, using {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireAtTurnEnd}.
     *
     * <p>The expiry turn is owner-relative: if the owner is the active player, their next turn is
     * two turns away; otherwise the upcoming turn is theirs. The permission is cleared at the end of
     * that turn by {@code TurnCleanupService}.
     */
    public void grantPlayUntilOwnersNextTurn(GameData gameData, UUID cardId, UUID ownerId) {
        int expireTurn = gameData.turnNumber + (ownerId.equals(gameData.activePlayerId) ? 2 : 1);
        gameData.exilePlayPermissions.put(cardId, ownerId);
        gameData.exilePlayPermissionsExpireAtTurnEnd.put(cardId, expireTurn);
    }

    /** Grants an owner permission to play a card from exile for as long as it remains exiled. */
    public void grantPlayWhileExiled(GameData gameData, UUID cardId, UUID ownerId) {
        gameData.exilePlayPermissions.put(cardId, ownerId);
    }

    /**
     * Grants an owner permission to play a card from exile and records a cost increase for players
     * who are opponents of {@code sourceControllerId}.
     */
    public void grantPlayWhileExiledWithOpponentTax(GameData gameData, UUID cardId, UUID ownerId,
                                                     UUID sourceControllerId, int amount) {
        grantPlayWhileExiled(gameData, cardId, ownerId);
        gameData.exilePlayCostModifiers.put(cardId,
                new ExilePlayCostModifier(ownerId, sourceControllerId, amount));
    }

    /**
     * Grants permission until the next end step belonging to {@code ownerId}. If that player's
     * current end step has already begun, the permission lasts through their following turn.
     */
    public void grantPlayUntilOwnersNextEndStep(GameData gameData, UUID cardId, UUID ownerId) {
        boolean ownerIsActive = ownerId.equals(gameData.activePlayerId);
        boolean currentEndStepHasBegun = gameData.currentStep != null
                && gameData.currentStep.ordinal() >= TurnStep.END_STEP.ordinal();
        int expireTurn = gameData.turnNumber + (ownerIsActive
                ? currentEndStepHasBegun ? 2 : 0
                : 1);
        gameData.exilePlayPermissions.put(cardId, ownerId);
        gameData.exilePlayPermissionsExpireAtTurnEnd.put(cardId, expireTurn);
    }

    public StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }

    /**
     * Handles the player's Knowledge Pool cast choice.
     * Called from GameService.handleMultipleCardsChosen dispatch.
     *
     * <p>The choice was begun while the Knowledge Pool trigger was resolving, so every path out of
     * here must end through the shared {@link InputCompletionService} epilogue: it resumes the stack
     * entry parked in {@code GameData.pendingEffectResolutionEntry}. Ending any other way leaves that
     * entry dangling, which wedges {@code GameData.deferPlayerLossCheck} so no player can ever lose
     * to a state-based action again. The one exception is the target-choice branch, which pauses for
     * more input and is resumed by {@code PermanentChoiceSpellHandlerService}.
     */
    public void handleKnowledgePoolCastChoice(GameData gameData, Player player, List<UUID> cardIds) {
        UUID playerId = player.getId();
        PendingKnowledgePoolCast pendingCast = gameData.pollPendingInteraction(PendingKnowledgePoolCast.class);
        UUID kpPermanentId = pendingCast != null ? pendingCast.sourcePermanentId() : null;

        // Clear interaction state
        gameData.interaction.clearAwaitingInput();

        if (cardIds == null || cardIds.isEmpty()) {
            // Player declined
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " declines to cast a spell from Knowledge Pool.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines Knowledge Pool cast", gameData.id, playerName);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID chosenCardId = cardIds.getFirst();

        // Find the chosen card in the KP pool
        List<Card> pool = gameData.getCardsExiledByPermanent(kpPermanentId);
        if (pool.isEmpty()) {
            log.warn("Game {} - Knowledge Pool pool not found for permanent {}", gameData.id, kpPermanentId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card chosenCard = null;
        for (Card c : pool) {
            if (c.getId().equals(chosenCardId)) {
                chosenCard = c;
                break;
            }
        }

        if (chosenCard == null) {
            log.warn("Game {} - Chosen card {} not found in Knowledge Pool", gameData.id, chosenCardId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (chosenCard.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(chosenCard,
                    " cannot be cast from exile and stays exiled."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Determine spell type
        StackEntryType spellType = mapCardTypeToSpellType(chosenCard);
        List<CardEffect> spellEffects = new ArrayList<>(chosenCard.getEffects(EffectSlot.SPELL));

        String playerName = gameData.playerIdToName.get(playerId);

        // If the spell needs a target, set up target selection
        if (EffectResolution.needsTarget(chosenCard)) {
            Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(chosenCard);
            List<UUID> validTargets = new ArrayList<>();

            // Only add permanents if the spell can actually target them
            if (allowedTargets.contains(TargetType.PERMANENT)) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (chosenCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                            if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                validTargets.add(p.getId());
                            }
                        } else if (gameQueryService.isCreature(gameData, p)) {
                            validTargets.add(p.getId());
                        }
                    }
                }
            }

            if (allowedTargets.contains(TargetType.PLAYER)) {
                validTargets.addAll(gameData.orderedPlayerIds);
            }

            if (validTargets.isEmpty()) {
                // It was never cast, and nothing in the oracle text moves it elsewhere: it stays
                // exiled with Knowledge Pool, so it can be chosen again by a later trigger.
                gameLogService.append(gameData, GameLog.cardThen(chosenCard,
                        " has no valid targets (Knowledge Pool) and stays exiled."));
                log.info("Game {} - {} Knowledge Pool cast has no valid targets", gameData.id, chosenCard.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            // Remove from exile now that it will be cast; the ExileCastSpellTarget flow puts it on
            // the stack. The unified list covers both KP-source tracking and player ownership.
            gameData.removeFromExile(chosenCard.getId());
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(chosenCard, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + chosenCard.getName() + ".");

            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", chosenCard,
                    " without paying its mana cost (Knowledge Pool) — choosing target."));
            log.info("Game {} - {} casts {} from Knowledge Pool, choosing target", gameData.id, playerName, chosenCard.getName());
            return;
        }

        // Non-targeted spell: put directly on stack
        gameData.removeFromExile(chosenCard.getId());
        gameData.stack.add(new StackEntry(
                spellType, chosenCard, playerId, chosenCard.getName(),
                spellEffects, 0, (UUID) null, null
        ));

        gameData.recordSpellCast(playerId, chosenCard);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", chosenCard,
                " without paying its mana cost (Knowledge Pool)."));
        log.info("Game {} - {} casts {} from Knowledge Pool without paying mana", gameData.id, playerName, chosenCard.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, chosenCard, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Handles the player's Mirror of Fate exiled card choice.
     * Called from GameService.handleMultipleCardsChosen dispatch.
     *
     * <p>The choice was begun mid-resolution, so unless the ordering prompt takes over this must end
     * through the shared {@link InputCompletionService} epilogue, which resumes the stack entry
     * parked in {@code GameData.pendingEffectResolutionEntry}. Ending any other way leaves that entry
     * dangling and wedges {@code GameData.deferPlayerLossCheck}.
     */
    public void handleMirrorOfFateChoice(GameData gameData, Player player, List<UUID> cardIds) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MirrorOfFateChoice.class) == null) {
            throw new IllegalStateException("Not awaiting Mirror of Fate choice");
        }
        PendingInteraction.MirrorOfFateChoice ctx =
                gameData.interaction.activeInteraction(PendingInteraction.MirrorOfFateChoice.class);
        if (ctx == null || !player.getId().equals(ctx.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Validate selected card IDs against valid set
        List<UUID> validIds = ctx.validCardIds();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }
        if (cardIds.size() > ctx.maxCount()) {
            throw new IllegalStateException("Too many cards selected (max " + ctx.maxCount() + ")");
        }

        gameData.interaction.clearAwaitingInput();

        exileLibraryAndPutChosenOnTop(gameData, player.getId(), cardIds);

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    /**
     * Exiles the controller's library and puts the chosen exiled cards back on top. Reached both
     * mid-resolution (nothing exiled, so nothing to choose) and from the choice above, so it never
     * runs a completion epilogue itself — asking for the ordering of two or more chosen cards is the
     * only thing it does beyond moving cards.
     */
    void exileLibraryAndPutChosenOnTop(GameData gameData, UUID controllerId, List<UUID> chosenCardIds) {
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Collect the chosen cards from the exile zone before exiling the library
        List<Card> playerExiled = gameData.getPlayerExiledCards(controllerId);
        List<Card> chosenCards = new ArrayList<>();
        for (UUID cardId : chosenCardIds) {
            for (Card c : playerExiled) {
                if (c.getId().equals(cardId)) {
                    chosenCards.add(c);
                    break;
                }
            }
        }

        // Exile all cards from the library
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library != null && !library.isEmpty()) {
            int exiledCount = library.size();
            for (Card card : library) {
                gameData.addToExile(controllerId, card);
            }
            library.clear();
            String exileLog = controllerName + " exiles " + exiledCount + " card"
                    + (exiledCount != 1 ? "s" : "") + " from their library (Mirror of Fate).";
            gameLogService.append(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} exiles {} cards from library (Mirror of Fate)",
                    gameData.id, controllerName, exiledCount);
        }

        // Remove the chosen cards from exile
        for (Card card : chosenCards) {
            gameData.removeFromExile(card.getId());
        }

        if (chosenCards.isEmpty()) {
            String emptyLog = controllerName + "'s library is now empty (Mirror of Fate).";
            gameLogService.append(gameData, GameLog.text(emptyLog));
        } else if (chosenCards.size() == 1) {
            // Single card: put directly on top, no ordering needed
            library.addFirst(chosenCards.getFirst());
            gameLogService.append(gameData, GameLog.textCardText(controllerName + " puts ",
                    chosenCards.getFirst(), " on top of their library (Mirror of Fate)."));
            log.info("Game {} - {} puts 1 exiled card on top of library (Mirror of Fate)",
                    gameData.id, controllerName);
        } else {
            // Multiple cards: player chooses the order via library reorder interaction
            String putLog = controllerName + " puts " + chosenCards.size()
                    + " cards on top of their library (Mirror of Fate) — choosing order.";
            gameLogService.append(gameData, GameLog.text(putLog));
            log.info("Game {} - {} puts {} exiled cards on top of library, awaiting order (Mirror of Fate)",
                    gameData.id, controllerName, chosenCards.size());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, chosenCards, false, controllerId,
                    "Put these cards on top of your library in any order (top to bottom)."));
            mutationCoordinator.invalidateAllPlayerViews(gameData);
        }
    }
}
