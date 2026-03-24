package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAtEndOfCombatAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.MirrorOfFateEffect;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;
    private final TriggerCollectionService triggerCollectionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;

    /**
     * Exiles one or more target permanents. Supports both single-target and multi-target modes.
     * Fizzles gracefully for any individual target that has already left the battlefield.
     */
    @HandlesEffect(ExileTargetPermanentEffect.class)
    void resolveExileTargetPermanent(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? List.of(entry.getTargetId())
                : entry.getTargetIds();

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
     * Exiles all permanents matching the effect's predicate filter across all battlefields.
     */
    @HandlesEffect(ExileAllPermanentsEffect.class)
    void resolveExileAllPermanents(GameData gameData, StackEntry entry, ExileAllPermanentsEffect effect) {
        List<Permanent> toExile = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(perm, effect.filter(), filterContext)) {
                    toExile.add(perm);
                }
            }
        });

        for (Permanent perm : toExile) {
            permanentRemovalService.removePermanentToExile(gameData, perm);
            String logEntry = perm.getCard().getName() + " is exiled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, perm.getCard().getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles all cards from all players' graveyards.
     */
    @HandlesEffect(ExileAllGraveyardsEffect.class)
    void resolveExileAllGraveyards(GameData gameData, StackEntry entry) {
        int totalExiled = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) continue;
            for (Card card : graveyard) {
                exileService.exileCard(gameData, playerId, card);
                totalExiled++;
            }
            graveyard.clear();
        }

        if (totalExiled > 0) {
            String logEntry = "All graveyards are exiled (" + totalExiled + " card"
                    + (totalExiled != 1 ? "s" : "") + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - All graveyards exiled ({} cards) by {}",
                    gameData.id, totalExiled, entry.getCard().getName());
        } else {
            String logEntry = "All graveyards are already empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - All graveyards already empty when {} resolved",
                    gameData.id, entry.getCard().getName());
        }
    }

    /**
     * Exiles the target creature and all other creatures on the battlefield with the same name.
     * Fizzles if the target creature has already left the battlefield.
     */
    @HandlesEffect(ExileTargetCreatureAndAllWithSameNameEffect.class)
    void resolveExileTargetCreatureAndAllWithSameName(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        String targetName = target.getCard().getName();

        // Collect all creatures with the same name across all battlefields (including the target)
        List<Permanent> toExile = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)
                        && perm.getCard().getName().equals(targetName)) {
                    toExile.add(perm);
                }
            }
        }

        for (Permanent perm : toExile) {
            permanentRemovalService.removePermanentToExile(gameData, perm);
            String logEntry = perm.getCard().getName() + " is exiled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, perm.getCard().getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles a permanent controlled by the player who was dealt combat damage.
     * Presents a multi-permanent choice filtered by the effect's predicate.
     * Context: StackEntry.targetId = damaged player ID.
     */
    @HandlesEffect(ExilePermanentDamagedPlayerControlsEffect.class)
    void resolveExilePermanentDamagedPlayerControls(GameData gameData, StackEntry entry,
                                                     ExilePermanentDamagedPlayerControlsEffect effect) {

        UUID defenderId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();

        if (defenderId == null) return;

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (effect.predicate() == null
                        || gameQueryService.matchesPermanentPredicate(gameData, perm, effect.predicate())) {
                    validIds.add(perm.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            String logEntry = entry.getCard().getName() + "'s ability resolves, but "
                    + gameData.playerIdToName.get(defenderId) + " has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.pendingExileDamagedPlayerControlsPermanent = true;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, 1,
                entry.getCard().getName() + "'s ability — Choose a permanent "
                        + gameData.playerIdToName.get(defenderId) + " controls to exile.");
    }

    /**
     * Exiles a target permanent and tracks the exiled card with the source permanent
     * via {@code permanentExiledCards}. Used by Karn Liberated whose abilities refer
     * to cards "exiled with" it.
     */
    @HandlesEffect(ExileTargetPermanentAndTrackWithSourceEffect.class)
    void resolveExileTargetPermanentAndTrackWithSource(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card exiledCard = target.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, target);

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
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
        }

        if (sourcePermanentId != null) {
            // removePermanentToExile already added to exile without source tracking;
            // remove that entry and re-add with source tracking
            var exiledEntry = gameData.findExiledCard(exiledCard.getId());
            UUID ownerId = exiledEntry != null ? exiledEntry.ownerId() : entry.getControllerId();
            gameData.removeFromExile(exiledCard.getId());
            gameData.addToExile(ownerId, exiledCard, sourcePermanentId);
        }

        String logEntry = exiledCard.getName() + " is exiled by " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} (tracked with source)",
                gameData.id, entry.getCard().getName(), exiledCard.getName());

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles a target permanent and imprints the exiled card onto the source permanent.
     * The exile is permanent (the card does NOT return when the source leaves).
     * Used by Exclusion Ritual.
     */
    @HandlesEffect(ExileTargetPermanentAndImprintEffect.class)
    void resolveExileTargetPermanentAndImprint(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card exiledCard = target.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, target);

        // Find the source permanent on the controller's battlefield and imprint
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard() == entry.getCard()) {
                    p.getCard().setImprintedCard(exiledCard);
                    break;
                }
            }
        }

        String logEntry = exiledCard.getName() + " is exiled by " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles and imprints {}",
                gameData.id, entry.getCard().getName(), exiledCard.getName());

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles a target permanent and schedules it to return to the battlefield under its owner's
     * control at the beginning of the next end step. Respects stolen-creature ownership.
     */
    @HandlesEffect(ExileTargetPermanentAndReturnAtEndStepEffect.class)
    void resolveExileTargetPermanentAndReturnAtEndStep(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);

        boolean returnTapped = entry.getEffectsToResolve().stream()
                .filter(e -> e instanceof ExileTargetPermanentAndReturnAtEndStepEffect)
                .map(e -> ((ExileTargetPermanentAndReturnAtEndStepEffect) e).returnTapped())
                .findFirst()
                .orElse(false);

        exileAndScheduleReturn(gameData, entry, target, ownerId, returnTapped);
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

        exileAndScheduleReturn(gameData, entry, source, entry.getControllerId(), false);
    }

    /**
     * Schedules the source permanent for exile-and-return-transformed at end of combat.
     * Used by Conqueror's Galleon and similar cards that exile themselves when attacking
     * and return transformed.
     */
    @HandlesEffect(ExileSelfAtEndOfCombatAndReturnTransformedEffect.class)
    void resolveExileSelfAtEndOfCombatAndReturnTransformed(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        gameData.pendingExileAndReturnTransformedAtEndOfCombat.add(source.getId());
        log.info("Game {} - {} scheduled for exile and return transformed at end of combat",
                gameData.id, source.getCard().getName());
    }

    private void exileAndScheduleReturn(GameData gameData, StackEntry entry,
                                        Permanent permanent, UUID ownerId, boolean returnTapped) {
        Card card = permanent.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, permanent);

        String logEntry = card.getName() + " is exiled. It will return at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {}; will return at next end step",
                gameData.id, entry.getCard().getName(), card.getName());

        gameData.pendingExileReturns.add(new PendingExileReturn(card, ownerId, returnTapped));

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles a target permanent until the source permanent leaves the battlefield. If the source
     * has already left before resolution, the target is still exiled but no return is tracked.
     * Respects stolen-creature ownership for the return.
     */
    @HandlesEffect(ExileTargetPermanentUntilSourceLeavesEffect.class)
    void resolveExileTargetPermanentUntilSourceLeaves(GameData gameData, StackEntry entry,
                                                      ExileTargetPermanentUntilSourceLeavesEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Find the source permanent on the battlefield by card reference
        UUID sourcePermanentId = null;
        Permanent sourcePermanent = null;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard() == entry.getCard()) {
                    sourcePermanentId = p.getId();
                    sourcePermanent = p;
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

        // Imprint the exiled card onto the source (e.g. Ixalan's Binding)
        if (effect.imprint() && sourcePermanent != null) {
            sourcePermanent.getCard().setImprintedCard(card);
        }

        String logEntry = card.getName() + " is exiled by " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} until it leaves the battlefield",
                gameData.id, entry.getCard().getName(), card.getName());

        if (sourcePermanentId != null) {
            gameData.exileReturnOnPermanentLeave.put(sourcePermanentId, new PendingExileReturn(card, ownerId));

            // Also add source tracking so AllowCastFromCardsExiledWithSourceEffect can find it
            var exiledEntry = gameData.findExiledCard(card.getId());
            if (exiledEntry != null && exiledEntry.sourcePermanentId() == null) {
                gameData.removeFromExile(card.getId());
                gameData.addToExile(ownerId, card, sourcePermanentId);
            }
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
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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
            // Find and remove from exile, tracking the owner
            var previousExiledEntry = gameData.findExiledCard(previouslyImprinted.getId());
            UUID previousOwnerId = previousExiledEntry != null ? previousExiledEntry.ownerId() : null;
            gameData.removeFromExile(previouslyImprinted.getId());
            // Return to owner's graveyard (the player whose exile zone it was in)
            UUID returnToId = previousOwnerId != null ? previousOwnerId : entry.getControllerId();
            graveyardService.addCardToGraveyard(gameData, returnToId, previouslyImprinted);
            String returnLog = previouslyImprinted.getName() + " returns to its owner's graveyard from exile.";
            gameBroadcastService.logAndBroadcast(gameData, returnLog);
            log.info("Game {} - Previously imprinted {} returned to graveyard", gameData.id, previouslyImprinted.getName());
        }

        // Remove dying card from graveyard
        gameData.playerGraveyards.get(graveyardOwnerId).remove(dyingCard);

        // Exile the dying card (add to card owner's exile zone)
        exileService.exileCard(gameData, graveyardOwnerId, dyingCard);

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
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null) continue;

            int toExile = Math.min(effect.count(), deck.size());
            List<String> exiledNames = new ArrayList<>();

            for (int i = 0; i < toExile; i++) {
                Card card = deck.removeFirst();
                exileService.exileCard(gameData, playerId, card, sourcePermanentId);
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

        exileService.exileCard(gameData, castingPlayerId, originalCard, kpPermanentId);

        String playerName = gameData.playerIdToName.get(castingPlayerId);
        String exileLog = playerName + " exiles " + originalCard.getName() + " (Knowledge Pool).";
        gameBroadcastService.logAndBroadcast(gameData, exileLog);
        log.info("Game {} - {} exiles {} to Knowledge Pool", gameData.id, playerName, originalCard.getName());

        // Step 4: Collect eligible cards — nonland, not the just-exiled card, from KP's pool
        List<Card> eligible = gameData.getCardsExiledByPermanent(kpPermanentId).stream()
                .filter(c -> !c.getId().equals(originalCard.getId()))
                .filter(c -> !c.hasType(CardType.LAND))
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
                            if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
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

    @HandlesEffect(OmenMachineDrawStepEffect.class)
    void resolveOmenMachineDrawStep(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} library empty for Omen Machine trigger", gameData.id, playerName);
            return;
        }

        // Exile the top card
        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, targetPlayerId, topCard);

        String exileLog = playerName + " exiles " + topCard.getName() + " (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, exileLog);
        log.info("Game {} - {} exiles {} (Omen Machine)", gameData.id, playerName, topCard.getName());

        if (topCard.hasType(CardType.LAND)) {
            // Land — put onto the battlefield
            gameData.removeFromExile(topCard.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, new Permanent(topCard));

            String landLog = playerName + " puts " + topCard.getName() + " onto the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, landLog);
            log.info("Game {} - {} puts {} onto battlefield (Omen Machine)", gameData.id, playerName, topCard.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, targetPlayerId, topCard, null, false);
        } else {
            // Non-land — cast without paying mana cost if able
            gameData.removeFromExile(topCard.getId());

            StackEntryType spellType = mapCardTypeToSpellType(topCard);
            List<CardEffect> spellEffects = new ArrayList<>(topCard.getEffects(EffectSlot.SPELL));

            if (EffectResolution.needsTarget(topCard)) {
                // Targeted spell — need to choose a target
                Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(topCard);
                List<UUID> validTargets = new ArrayList<>();

                // Only add permanents if the spell can actually target them
                if (allowedTargets.contains(TargetType.PERMANENT)) {
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (topCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
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
                    // Can't cast — card stays in exile
                    exileService.exileCard(gameData, targetPlayerId, topCard);
                    String noTargetLog = topCard.getName() + " has no valid targets and remains in exile.";
                    gameBroadcastService.logAndBroadcast(gameData, noTargetLog);
                    log.info("Game {} - {} can't be cast (no targets), stays in exile", gameData.id, topCard.getName());
                    return;
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ExileCastSpellTarget(topCard, targetPlayerId, spellEffects, spellType));
                playerInputService.beginPermanentChoice(gameData, targetPlayerId, validTargets,
                        "Choose a target for " + topCard.getName() + ".");

                String castLog = playerName + " casts " + topCard.getName() + " without paying its mana cost — choosing target.";
                gameBroadcastService.logAndBroadcast(gameData, castLog);
                log.info("Game {} - {} casts {} (Omen Machine), choosing target", gameData.id, playerName, topCard.getName());
            } else {
                // Non-targeted spell — put directly on stack
                gameData.stack.add(new StackEntry(
                        spellType, topCard, targetPlayerId, topCard.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.recordSpellCast(targetPlayerId, topCard);
                gameData.priorityPassedBy.clear();

                String castLog = playerName + " casts " + topCard.getName() + " without paying its mana cost.";
                gameBroadcastService.logAndBroadcast(gameData, castLog);
                log.info("Game {} - {} casts {} (Omen Machine) without paying mana", gameData.id, playerName, topCard.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, topCard, targetPlayerId, false);
            }
        }
    }

    @HandlesEffect(MirrorOfFateEffect.class)
    void resolveMirrorOfFate(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        List<Card> exiledCards = gameData.getPlayerExiledCards(controllerId);
        if (exiledCards.isEmpty()) {
            // No exiled cards to choose — just exile the entire library
            exileLibraryAndPutChosenOnTop(gameData, controllerId, List.of());
            return;
        }

        // Present up to 7 face-up exiled cards the player owns for selection
        int maxCount = Math.min(7, exiledCards.size());
        List<UUID> validCardIds = exiledCards.stream().map(Card::getId).toList();
        List<CardView> cardViews = exiledCards.stream().map(cardViewFactory::create).toList();

        gameData.interaction.beginMirrorOfFateChoice(controllerId, new HashSet<>(validCardIds), maxCount);
        playerInputService.sendMirrorOfFateChoice(gameData, controllerId, validCardIds, cardViews, maxCount);

        log.info("Game {} - Awaiting {} to choose exiled cards for Mirror of Fate (up to {})",
                gameData.id, controllerName, maxCount);
    }

    /**
     * Handles the player's Mirror of Fate exiled card choice.
     * Called from GameService.handleMultipleGraveyardCardsChosen dispatch.
     */
    public void handleMirrorOfFateChoice(GameData gameData, Player player, List<UUID> cardIds) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MIRROR_OF_FATE_CHOICE)) {
            throw new IllegalStateException("Not awaiting Mirror of Fate choice");
        }
        InteractionContext.MirrorOfFateChoice ctx = gameData.interaction.mirrorOfFateChoiceContext();
        if (ctx == null || !player.getId().equals(ctx.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Validate selected card IDs against valid set
        Set<UUID> validIds = ctx.validCardIds();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }
        if (cardIds.size() > ctx.maxCount()) {
            throw new IllegalStateException("Too many cards selected (max " + ctx.maxCount() + ")");
        }

        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMirrorOfFateChoice();

        exileLibraryAndPutChosenOnTop(gameData, player.getId(), cardIds);
    }

    private void exileLibraryAndPutChosenOnTop(GameData gameData, UUID controllerId, List<UUID> chosenCardIds) {
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
            playerInputService.beginLibraryReorderFromExile(gameData, controllerId, chosenCards);
            gameBroadcastService.broadcastGameState(gameData);
        }
    }

    /**
     * Exiles the top card of the controller's library.
     * Used by Precognition Field's "{3}: Exile the top card of your library."
     * When {@code trackWithSource} is true, also tracks the exiled card in
     * {@code permanentExiledCards} under the source permanent (e.g. Rona, Disciple of Gix).
     */
    @HandlesEffect(ExileTopCardOfOwnLibraryEffect.class)
    void resolveExileTopCardOfOwnLibrary(GameData gameData, StackEntry entry,
                                         ExileTopCardOfOwnLibraryEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = controllerName + "'s library is empty — nothing to exile.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        Card topCard = deck.removeFirst();

        if (effect.trackWithSource()) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                exileService.exileCard(gameData, controllerId, topCard, sourcePermanentId);
            } else {
                exileService.exileCard(gameData, controllerId, topCard);
            }
        } else {
            exileService.exileCard(gameData, controllerId, topCard);
        }

        String logEntry = controllerName + " exiles " + topCard.getName() + " from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} from library top", gameData.id, controllerName, topCard.getName());
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

