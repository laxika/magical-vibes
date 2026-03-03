package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Resolves exile-related effects: permanent exile, temporary exile with scheduled return,
 * exile-until-source-leaves, and imprint mechanics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExileResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;
    private final TriggerCollectionService triggerCollectionService;

    /**
     * Exiles one or more target permanents. Supports both single-target and multi-target modes.
     * Fizzles gracefully for any individual target that has already left the battlefield.
     */
    @HandlesEffect(ExileTargetPermanentEffect.class)
    void resolveExileTargetPermanent(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetPermanentIds().isEmpty()
                ? List.of(entry.getTargetPermanentId())
                : entry.getTargetPermanentIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            permanentRemovalService.removePermanentToExile(gameData, target);
            String logEntry = target.getCard().getName() + " is exiled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles a target permanent and schedules it to return to the battlefield under its owner's
     * control at the beginning of the next end step. Respects stolen-creature ownership.
     */
    @HandlesEffect(ExileTargetPermanentAndReturnAtEndStepEffect.class)
    void resolveExileTargetPermanentAndReturnAtEndStep(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);

        exileAndScheduleReturn(gameData, entry, target, ownerId);
    }

    /**
     * Exiles the source permanent itself and schedules it to return to the battlefield under its
     * controller's control at the beginning of the next end step.
     */
    @HandlesEffect(ExileSelfAndReturnAtEndStepEffect.class)
    void resolveExileSelfAndReturnAtEndStep(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        exileAndScheduleReturn(gameData, entry, source, entry.getControllerId());
    }

    private void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId) {
        Card card = permanent.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, permanent);

        String logEntry = card.getName() + " is exiled. It will return at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {}; will return at next end step",
                gameData.id, entry.getCard().getName(), card.getName());

        gameData.pendingExileReturns.add(new PendingExileReturn(card, ownerId));

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles a target permanent until the source permanent leaves the battlefield. If the source
     * has already left before resolution, the target is still exiled but no return is tracked.
     * Respects stolen-creature ownership for the return.
     */
    @HandlesEffect(ExileTargetPermanentUntilSourceLeavesEffect.class)
    void resolveExileTargetPermanentUntilSourceLeaves(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        // Find the source permanent on the battlefield by card reference
        UUID sourcePermanentId = null;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard() == entry.getCard()) {
                    sourcePermanentId = p.getId();
                    break;
                }
            }
        }

        if (sourcePermanentId == null) {
            // Source already left the battlefield — exile still happens but no return tracking
            log.info("Game {} - Source permanent for {} no longer on battlefield, exile without return tracking",
                    gameData.id, entry.getCard().getName());
        }

        Card card = target.getOriginalCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);

        permanentRemovalService.removePermanentToExile(gameData, target);

        String logEntry = card.getName() + " is exiled by " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} until it leaves the battlefield",
                gameData.id, entry.getCard().getName(), card.getName());

        if (sourcePermanentId != null) {
            gameData.exileReturnOnPermanentLeave.put(sourcePermanentId, new PendingExileReturn(card, ownerId));
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Imprints a dying creature onto a source permanent (e.g. Mimic Vat). Moves the creature card
     * from its owner's graveyard to exile and sets it as the imprinted card. If a card was
     * previously imprinted, it is returned to its owner's graveyard first.
     */
    @HandlesEffect(ImprintDyingCreatureEffect.class)
    void resolveImprintDyingCreature(GameData gameData, StackEntry entry, ImprintDyingCreatureEffect effect) {
        UUID dyingCardId = effect.dyingCardId();
        if (dyingCardId == null) return;

        // Find the source permanent (Mimic Vat) on the battlefield
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Mimic Vat no longer on battlefield, imprint fizzles", gameData.id);
            return;
        }

        // Find the dying card in any graveyard
        Card dyingCard = null;
        UUID graveyardOwnerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card c : graveyard) {
                if (c.getId().equals(dyingCardId)) {
                    dyingCard = c;
                    graveyardOwnerId = playerId;
                    break;
                }
            }
            if (dyingCard != null) break;
        }

        if (dyingCard == null) {
            log.info("Game {} - Dying card no longer in any graveyard, imprint fizzles", gameData.id);
            return;
        }

        // Return previously imprinted card to its owner's graveyard
        Card previouslyImprinted = sourcePermanent.getCard().getImprintedCard();
        if (previouslyImprinted != null) {
            // Find and remove from whichever exile zone it's in, tracking the owner
            UUID previousOwnerId = null;
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Card> exile = gameData.playerExiledCards.get(playerId);
                if (exile != null && exile.remove(previouslyImprinted)) {
                    previousOwnerId = playerId;
                    break;
                }
            }
            // Return to owner's graveyard (the player whose exile zone it was in)
            UUID returnToId = previousOwnerId != null ? previousOwnerId : entry.getControllerId();
            gameHelper.addCardToGraveyard(gameData, returnToId, previouslyImprinted);
            String returnLog = previouslyImprinted.getName() + " returns to its owner's graveyard from exile.";
            gameBroadcastService.logAndBroadcast(gameData, returnLog);
            log.info("Game {} - Previously imprinted {} returned to graveyard", gameData.id, previouslyImprinted.getName());
        }

        // Remove dying card from graveyard
        gameData.playerGraveyards.get(graveyardOwnerId).remove(dyingCard);

        // Exile the dying card (add to card owner's exile zone)
        gameData.playerExiledCards.get(graveyardOwnerId).add(dyingCard);

        // Set as imprinted on the source permanent
        sourcePermanent.getCard().setImprintedCard(dyingCard);

        String logMsg = dyingCard.getName() + " is exiled and imprinted on " + sourcePermanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} imprinted on {}", gameData.id, dyingCard.getName(), sourcePermanent.getCard().getName());
    }

    /**
     * Prompts the controller to choose a card from their hand matching the effect's filter to exile
     * and imprint onto the source permanent (e.g. Semblance Anvil). Skips if no matching cards
     * are available.
     */
    @HandlesEffect(ExileFromHandToImprintEffect.class)
    void resolveExileFromHandToImprint(GameData gameData, StackEntry entry, ExileFromHandToImprintEffect effect) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield, imprint from hand fizzles", gameData.id);
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            log.info("Game {} - Controller has no cards in hand, imprint from hand skipped", gameData.id);
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (gameQueryService.matchesCardPredicate(hand.get(i), effect.filter(), null)) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            log.info("Game {} - {} has no matching cards in hand, imprint from hand skipped", gameData.id, playerName);
            return;
        }

        playerInputService.beginImprintFromHandChoice(gameData, controllerId, validIndices,
                "Choose " + effect.description() + " from your hand to exile and imprint.", sourcePermanent.getId());
    }

    @HandlesEffect(EachPlayerExilesTopCardsToSourceEffect.class)
    void resolveEachPlayerExilesTopCardsToSource(GameData gameData, StackEntry entry, EachPlayerExilesTopCardsToSourceEffect effect) {
        // Find the source permanent (Knowledge Pool) on the battlefield
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId != null ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;

        // Fallback: find permanent by card ID on controller's battlefield
        if (sourcePermanent == null) {
            UUID controllerId = entry.getControllerId();
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(entry.getCard().getId())) {
                        sourcePermanent = p;
                        sourcePermanentId = p.getId();
                        break;
                    }
                }
            }
        }

        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield, exile-top-cards fizzles", gameData.id);
            return;
        }

        // Initialize the per-permanent exile list if needed
        List<Card> pool = gameData.permanentExiledCards.computeIfAbsent(sourcePermanentId,
                k -> Collections.synchronizedList(new ArrayList<>()));

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null) continue;

            int toExile = Math.min(effect.count(), deck.size());
            List<String> exiledNames = new ArrayList<>();

            for (int i = 0; i < toExile; i++) {
                Card card = deck.removeFirst();
                pool.add(card);
                gameData.playerExiledCards.get(playerId).add(card);
                exiledNames.add(card.getName());
            }

            if (!exiledNames.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames)
                        + " from the top of their library (" + sourcePermanent.getCard().getName() + ").";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiles {} cards from library to {}",
                        gameData.id, playerName, toExile, sourcePermanent.getCard().getName());
            }
        }
    }

    @HandlesEffect(KnowledgePoolExileAndCastEffect.class)
    void resolveKnowledgePoolExileAndCast(GameData gameData, StackEntry entry, KnowledgePoolExileAndCastEffect effect) {
        UUID kpPermanentId = effect.knowledgePoolPermanentId();

        // Step 1: Verify KP permanent still on battlefield
        Permanent kpPermanent = gameQueryService.findPermanentById(gameData, kpPermanentId);
        if (kpPermanent == null) {
            log.info("Game {} - Knowledge Pool no longer on battlefield, trigger fizzles", gameData.id);
            return;
        }

        // Step 2: Find original spell on stack by card ID
        UUID originalSpellCardId = effect.originalSpellCardId();
        StackEntry originalSpell = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(originalSpellCardId)) {
                originalSpell = se;
                break;
            }
        }

        if (originalSpell == null) {
            // "If the player does" fails — original spell already gone (countered or exiled by another KP)
            log.info("Game {} - Original spell no longer on stack, Knowledge Pool 'if the player does' fails", gameData.id);
            String logEntry = "Knowledge Pool's ability — original spell is no longer on the stack.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Step 3: Remove original spell from stack, add to KP pool + player exile
        // "that player" = the player who cast the original spell (not the KP controller)
        UUID castingPlayerId = effect.castingPlayerId();
        Card originalCard = originalSpell.getCard();
        gameData.stack.remove(originalSpell);

        List<Card> pool = gameData.permanentExiledCards.computeIfAbsent(kpPermanentId,
                k -> Collections.synchronizedList(new ArrayList<>()));
        pool.add(originalCard);
        gameData.playerExiledCards.get(castingPlayerId).add(originalCard);

        String playerName = gameData.playerIdToName.get(castingPlayerId);
        String exileLog = playerName + " exiles " + originalCard.getName() + " (Knowledge Pool).";
        gameBroadcastService.logAndBroadcast(gameData, exileLog);
        log.info("Game {} - {} exiles {} to Knowledge Pool", gameData.id, playerName, originalCard.getName());

        // Step 4: Collect eligible cards — nonland, not the just-exiled card, from KP's pool
        List<Card> eligible = pool.stream()
                .filter(c -> !c.getId().equals(originalCard.getId()))
                .filter(c -> c.getType() != CardType.LAND)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            String noChoiceLog = "Knowledge Pool — no other nonland cards exiled. " + playerName + " cannot cast a spell.";
            gameBroadcastService.logAndBroadcast(gameData, noChoiceLog);
            log.info("Game {} - No eligible cards in Knowledge Pool for {}", gameData.id, playerName);
            return;
        }

        // Step 5: Present choice to the player
        gameData.knowledgePoolSourcePermanentId = kpPermanentId;

        List<UUID> validCardIds = eligible.stream().map(Card::getId).toList();
        List<CardView> cardViews = eligible.stream().map(cardViewFactory::create).toList();

        gameData.interaction.beginKnowledgePoolCastChoice(castingPlayerId, new java.util.HashSet<>(validCardIds), 1);
        playerInputService.sendKnowledgePoolCastChoice(gameData, castingPlayerId, validCardIds, cardViews);
    }

    /**
     * Handles the player's Knowledge Pool cast choice.
     * Called from GameService.handleMultipleGraveyardCardsChosen dispatch.
     */
    public void handleKnowledgePoolCastChoice(GameData gameData, com.github.laxika.magicalvibes.model.Player player, List<UUID> cardIds) {
        UUID playerId = player.getId();
        UUID kpPermanentId = gameData.knowledgePoolSourcePermanentId;

        // Clear interaction state
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearKnowledgePoolCastChoice();
        gameData.knowledgePoolSourcePermanentId = null;

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
        List<Card> pool = gameData.permanentExiledCards.get(kpPermanentId);
        if (pool == null) {
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

        // Remove from KP pool and player exile
        pool.remove(chosenCard);
        gameData.playerExiledCards.get(playerId).remove(chosenCard);

        // Determine spell type
        StackEntryType spellType = mapCardTypeToSpellType(chosenCard);
        List<CardEffect> spellEffects = new ArrayList<>(chosenCard.getEffects(EffectSlot.SPELL));

        String playerName = gameData.playerIdToName.get(playerId);

        // If the spell needs a target, set up target selection
        if (chosenCard.isNeedsTarget()) {
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (chosenCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
            boolean canTargetPlayer = spellEffects.stream().anyMatch(CardEffect::canTargetPlayer);
            if (canTargetPlayer) {
                validTargets.addAll(gameData.orderedPlayerIds);
            }

            if (validTargets.isEmpty()) {
                // No valid targets — card goes to graveyard
                gameHelper.addCardToGraveyard(gameData, playerId, chosenCard);
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

        gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
        gameData.priorityPassedBy.clear();

        String logEntry = playerName + " casts " + chosenCard.getName() + " without paying its mana cost (Knowledge Pool).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} from Knowledge Pool without paying mana", gameData.id, playerName, chosenCard.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, chosenCard, playerId, false);
        gameBroadcastService.broadcastGameState(gameData);
    }

    private StackEntryType mapCardTypeToSpellType(Card card) {
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
}

