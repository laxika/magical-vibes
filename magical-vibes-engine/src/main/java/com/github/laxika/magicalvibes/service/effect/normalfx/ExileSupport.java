package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
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
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class ExileSupport {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    public void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId, boolean returnTapped) {
        exileAndScheduleReturn(gameData, entry, permanent, ownerId, returnTapped, TurnStep.END_STEP);
    }

    public void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId, boolean returnTapped,
                                        TurnStep returnStep) {
        Card card = permanent.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, permanent);

        String logEntry = card.getName() + " is exiled. It will return at the beginning of the next "
                + returnStep.getDisplayName().toLowerCase() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {}; will return at next {}",
                gameData.id, entry.getCard().getName(), card.getName(), returnStep);

        gameData.pendingExileReturns.add(new PendingExileReturn(card, ownerId, returnTapped, false, returnStep));

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    public StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }

    /**
     * Handles the player's Knowledge Pool cast choice.
     * Called from GameService.handleMultipleCardsChosen dispatch.
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines Knowledge Pool cast", gameData.id, playerName);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }

        UUID chosenCardId = cardIds.getFirst();

        // Find the chosen card in the KP pool
        List<Card> pool = gameData.getCardsExiledByPermanent(kpPermanentId);
        if (pool.isEmpty()) {
            log.warn("Game {} - Knowledge Pool pool not found for permanent {}", gameData.id, kpPermanentId);
            gameBroadcastService.broadcastGameState(gameData);
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
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }

        // Remove from exile (unified list covers both KP-source tracking and player ownership)
        gameData.removeFromExile(chosenCard.getId());

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
                // No valid targets — card goes to graveyard
                graveyardService.addCardToGraveyard(gameData, playerId, chosenCard);
                String logEntry = chosenCard.getName() + " has no valid targets (Knowledge Pool). It is put into the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} Knowledge Pool cast has no valid targets", gameData.id, chosenCard.getName());
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(chosenCard, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + chosenCard.getName() + ".");

            String logEntry = playerName + " casts " + chosenCard.getName() + " without paying its mana cost (Knowledge Pool) — choosing target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} casts {} from Knowledge Pool, choosing target", gameData.id, playerName, chosenCard.getName());
            return;
        }

        // Non-targeted spell: put directly on stack
        gameData.stack.add(new StackEntry(
                spellType, chosenCard, playerId, chosenCard.getName(),
                spellEffects, 0, (UUID) null, null
        ));

        gameData.recordSpellCast(playerId, chosenCard);
        gameData.priorityPassedBy.clear();

        String logEntry = playerName + " casts " + chosenCard.getName() + " without paying its mana cost (Knowledge Pool).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} from Knowledge Pool without paying mana", gameData.id, playerName, chosenCard.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, chosenCard, playerId, false);
        gameBroadcastService.broadcastGameState(gameData);
    }

    /**
     * Handles the player's Mirror of Fate exiled card choice.
     * Called from GameService.handleMultipleCardsChosen dispatch.
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
    }

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
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            log.info("Game {} - {} exiles {} cards from library (Mirror of Fate)",
                    gameData.id, controllerName, exiledCount);
        }

        // Remove the chosen cards from exile
        for (Card card : chosenCards) {
            gameData.removeFromExile(card.getId());
        }

        if (chosenCards.isEmpty()) {
            String emptyLog = controllerName + "'s library is now empty (Mirror of Fate).";
            gameBroadcastService.logAndBroadcast(gameData, emptyLog);
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
        } else if (chosenCards.size() == 1) {
            // Single card: put directly on top, no ordering needed
            library.addFirst(chosenCards.getFirst());
            String putLog = controllerName + " puts " + chosenCards.getFirst().getName()
                    + " on top of their library (Mirror of Fate).";
            gameBroadcastService.logAndBroadcast(gameData, putLog);
            log.info("Game {} - {} puts 1 exiled card on top of library (Mirror of Fate)",
                    gameData.id, controllerName);
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
        } else {
            // Multiple cards: player chooses the order via library reorder interaction
            String putLog = controllerName + " puts " + chosenCards.size()
                    + " cards on top of their library (Mirror of Fate) — choosing order.";
            gameBroadcastService.logAndBroadcast(gameData, putLog);
            log.info("Game {} - {} puts {} exiled cards on top of library, awaiting order (Mirror of Fate)",
                    gameData.id, controllerName, chosenCards.size());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, chosenCards, false, controllerId,
                    "Put these cards on top of your library in any order (top to bottom)."));
            gameBroadcastService.broadcastGameState(gameData);
        }
    }
}
