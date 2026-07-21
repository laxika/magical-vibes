package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Shared graveyard return/exile helpers used by every normal Graveyard Return effect handler
 * and by input handlers (graveyard choice, may ability pile separation).
 *
 * <p>Extracted verbatim from {@code GraveyardReturnResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardReturnSupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final LifeSupport lifeSupport;
    private final ExileService exileService;
    private final CardViewFactory cardViewFactory;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Resolves a {@link ReturnCardFromGraveyardEffect} by returning one or more cards from a graveyard
     * to the controller's hand or battlefield. Handles three resolution paths:
     * <ol>
     *   <li>Pre-targeted: the card was already targeted during casting (includes aura attachment).</li>
     *   <li>Return all: returns every matching card without player choice.</li>
     *   <li>Search and choose: prompts the controller to pick a card from their own or all graveyards.</li>
     * </ol>
     * Fizzles if targeted cards are no longer valid. May also grant life equal to the returned card's
     * mana value when the effect specifies it.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the graveyard return effect configuration
     */

    /** Appends {@code cards} to {@code builder} as comma-separated card segments. */
    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }

    public void resolvePreTargeted(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                    UUID controllerId, UUID sourceCardId) {
        resolvePreTargetedById(gameData, entry, effect, controllerId, sourceCardId, entry.getTargetId());
    }

    public void resolvePreTargetedById(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                        UUID controllerId, UUID sourceCardId, UUID targetCardId) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (targetCard == null || (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(targetCard, effect.filter(), sourceCardId))) {
            String fizzleLog = entry.getDescription() + " fizzles (target " + filterLabel + " is no longer in a graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(fizzleLog));
            return;
        }

        // Aura attachment path
        if (effect.attachmentTarget() != null) {
            List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
            List<UUID> attachTargetIds = new ArrayList<>();
            if (controllerBf != null) {
                for (Permanent p : controllerBf) {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.attachmentTarget())) {
                        attachTargetIds.add(p.getId());
                    }
                }
            }

            if (attachTargetIds.isEmpty()) {
                String fizzleLog = entry.getDescription() + " fizzles (no creatures to attach Aura to).";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(fizzleLog));
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
            gameData.interaction.setPendingAuraCard(targetCard);

            playerInputService.beginPermanentChoice(gameData, controllerId, attachTargetIds,
                    "Choose a creature you control to attach " + targetCard.getName() + " to.");
            return;
        }

        // A card returned to HAND or to the top of a library always goes to its owner's zone
        // (only BATTLEFIELD returns can put a card under a non-owner's control). Resolve the
        // graveyard owner before removal.
        UUID destinationPlayerId = controllerId;
        if (effect.destination() == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY
                || effect.destination() == GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY
                || effect.destination() == GraveyardChoiceDestination.HAND) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
            if (ownerId != null) {
                destinationPlayerId = ownerId;
            }
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

        if ((effect.grantHaste() || effect.exileAtEndStep())
                && effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
            putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, targetCard,
                    effect.grantHaste(), effect.exileAtEndStep());
        } else {
            moveCardToDestination(gameData, destinationPlayerId, targetCard, effect.destination(),
                    effect.grantColor(), effect.grantSubtype(), effect.enterTapped());
        }

        if (effect.enterWithMannequinCounter()
                && effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().getId().equals(targetCard.getId())) {
                        p.setCounterCount(CounterType.MANNEQUIN, 1);
                        break;
                    }
                }
            }
        }

        if (effect.gainLifeEqualToManaValue()) {
            applyLifeGainEqualToManaValue(gameData, controllerId, targetCard);
        }
    }

    public void resolveReturnAll(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                  UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (effect.thisTurnOnly() || effect.fromBattlefieldThisTurn() || effect.fromAnywhereThisTurn()
                || effect.discardedOrCycledThisTurn()) {
            Set<UUID> trackedIds;
            String sourceLabel;
            if (effect.discardedOrCycledThisTurn()) {
                trackedIds = gameData.cardsDiscardedOrCycledThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "by cycling or discarding";
            } else if (effect.fromAnywhereThisTurn()) {
                trackedIds = gameData.cardsPutIntoGraveyardFromAnywhereThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "from anywhere";
            } else if (effect.fromBattlefieldThisTurn()) {
                trackedIds = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "from the battlefield";
            } else {
                trackedIds = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "from the battlefield";
            }

            if (graveyard == null || graveyard.isEmpty() || trackedIds.isEmpty()) {
                String logEntry = entry.getDescription() + " - no cards were put into your graveyard " + sourceLabel + " this turn.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                return;
            }

            List<Card> toReturn = new ArrayList<>();
            for (Card card : graveyard) {
                if (!card.isToken()
                        && trackedIds.contains(card.getId())
                        && predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                    toReturn.add(card);
                }
            }

            if (toReturn.isEmpty()) {
                String logEntry = entry.getDescription() + " - no cards were put into your graveyard " + sourceLabel + " this turn.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                return;
            }

            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : toReturn) {
                    graveyard.remove(card);
                    gameData.addCardToHand(controllerId, card);
                    trackedIds.remove(card.getId());
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + " returns ");
            appendCardList(builder, toReturn);
            builder.text(" from graveyard to hand.");
            gameBroadcastService.logAndBroadcast(gameData, builder.build());
            log.info("Game {} - {} returns {} card(s) from graveyard to hand",
                    gameData.id, playerName, toReturn.size());
            return;
        }

        // Return all matching — collect from either controller's graveyard or all graveyards
        Map<UUID, List<Card>> graveyardsToSearch = new LinkedHashMap<>();
        if (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            for (Map.Entry<UUID, List<Card>> gyEntry : gameData.playerGraveyards.entrySet()) {
                if (gyEntry.getValue() != null && !gyEntry.getValue().isEmpty()) {
                    graveyardsToSearch.put(gyEntry.getKey(), gyEntry.getValue());
                }
            }
        } else {
            if (graveyard != null && !graveyard.isEmpty()) {
                graveyardsToSearch.put(controllerId, graveyard);
            }
        }

        if (graveyardsToSearch.isEmpty()) {
            return;
        }

        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Map.Entry<UUID, List<Card>> gyEntry : graveyardsToSearch.entrySet()) {
                List<Card> gy = gyEntry.getValue();
                List<Card> toReturn = new ArrayList<>();
                for (Card card : gy) {
                    if (predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                        toReturn.add(card);
                    }
                }
                for (Card card : toReturn) {
                    gy.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, gyEntry.getKey());
                    UUID targetPlayerId = (effect.destination() == GraveyardChoiceDestination.HAND || effect.underOwnersControl())
                            ? gyEntry.getKey() : controllerId;
                    if (effect.destination() == GraveyardChoiceDestination.HAND) {
                        gameData.addCardToHand(targetPlayerId, card);
                    } else if (effect.grantHaste() || effect.exileAtEndStep()) {
                        putCardOntoBattlefieldWithHasteAndExile(gameData, targetPlayerId, card,
                                effect.grantHaste(), effect.exileAtEndStep());
                    } else {
                        putCardOntoBattlefield(gameData, targetPlayerId, card, effect.grantColor(), effect.grantSubtype(),
                                effect.enterTapped(), effect.enterAttacking());
                    }
                    returnedCards.add(card);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (returnedCards.isEmpty()) {
            return;
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destName = effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
        appendCardList(builder, returnedCards);
        builder.text(" onto " + destName + " from "
                + (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS ? "all graveyards" : "graveyard") + ".");
        gameBroadcastService.logAndBroadcast(gameData, builder.build());
        log.info("Game {} - {} puts {} onto {} from graveyards", gameData.id, playerName,
                returnedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse(""), destName);
    }

    public void resolveReturnAtRandom(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                        UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        // Exile the source card from graveyard before selecting random cards (e.g. Moldgraf Monstrosity)
        if (effect.exileSourceFromGraveyard() && graveyard != null && sourceCardId != null) {
            Card sourceCard = graveyard.stream()
                    .filter(c -> c.getId().equals(sourceCardId))
                    .findFirst()
                    .orElse(null);
            if (sourceCard != null) {
                graveyard.remove(sourceCard);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                exileService.exileCard(gameData, controllerId, sourceCard);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " exiles " , sourceCard, " from graveyard."));
                log.info("Game {} - {} exiles {} from graveyard", gameData.id, playerName, sourceCard.getName());
            }
        }

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        List<Card> matchingCards = new ArrayList<>();
        for (Card card : graveyard) {
            if (predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                matchingCards.add(card);
            }
        }

        if (matchingCards.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        int count = Math.min(effect.randomCount(), matchingCards.size());
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int i = 0; i < count; i++) {
                Card randomCard = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
                matchingCards.remove(randomCard);
                graveyard.remove(randomCard);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);

                if (effect.destination() == GraveyardChoiceDestination.HAND) {
                    gameData.addCardToHand(controllerId, randomCard);
                } else {
                    putCardOntoBattlefield(gameData, controllerId, randomCard, null, null, effect.enterTapped());
                }
                returnedCards.add(randomCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        GameLog.Builder builder = GameLog.builder().text(playerName + " returns ");
        appendCardList(builder, returnedCards);
        builder.text(" at random from graveyard to " + destText + ".");
        gameBroadcastService.logAndBroadcast(gameData, builder.build());
        log.info("Game {} - {} returns {} at random from graveyard to {}",
                gameData.id, playerName, returnedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse(""), destText);
    }

    public void resolveFromControllersGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                                 UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), effect.filter(), sourceCardId)) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        // Greatest-power restriction (e.g. Desecrator Hag): keep only the matching card(s) tied for
        // the greatest power. A single such card is a forced return; ties let the controller choose.
        if (effect.greatestPower()) {
            int maxPower = matchingIndices.stream()
                    .map(i -> graveyard.get(i).getPower())
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(Integer.MIN_VALUE);
            matchingIndices = matchingIndices.stream()
                    .filter(i -> graveyard.get(i).getPower() != null && graveyard.get(i).getPower() == maxPower)
                    .toList();
        }

        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield";
        String prompt = "Return a " + filterLabel + " from your graveyard to " + destText + ".";

        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, effect.destination(), prompt)
                .mandatory(effect.greatestPower())
                .gainLifeEqualToManaValue(effect.gainLifeEqualToManaValue());
        if (effect.grantColor() != null) {
            choice.grantColor(effect.grantColor());
        }
        if (effect.grantSubtype() != null) {
            choice.grantSubtype(effect.grantSubtype());
        }
        if (effect.grantSourceHasteIfSubtype() != null) {
            choice.grantSourceHasteIfSubtype(effect.grantSourceHasteIfSubtype(), entry.getSourcePermanentId());
        }

        if (effect.attachToSource()) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(sourceCardId)) {
                        choice.attachToSourcePermanentId(p.getId());
                        break;
                    }
                }
            }
        }

        interactionHandlerRegistry.begin(gameData, choice.build());
    }

    public void resolveFromAllGraveyards(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                          UUID controllerId, UUID sourceCardId) {
        List<Card> cardPool = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                    cardPool.add(card);
                }
            }
        }

        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (cardPool.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in any graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            entry.getEffectsToResolve().removeIf(e -> e instanceof ShuffleIntoLibraryEffect);
            return;
        }

        List<Integer> indices = IntStream.range(0, cardPool.size()).boxed().toList();

        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield under your control";
        String prompt = "Choose a " + filterLabel + " from a graveyard to put onto " + destText + ".";

        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(controllerId, indices, effect.destination(), prompt)
                .cardPool(cardPool);
        if (effect.grantColor() != null) {
            choice.grantColor(effect.grantColor());
        }
        if (effect.grantSubtype() != null) {
            choice.grantSubtype(effect.grantSubtype());
        }
        interactionHandlerRegistry.begin(gameData, choice.build());
    }

    /**
     * Resolves a {@link PutTargetCardsFromGraveyardOnTopOfLibraryEffect} by moving each pre-targeted
     * card from the controller's graveyard to the top of their library. Cards are placed in target
     * order, so the last processed card ends up on top.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the top-of-library effect configuration
     */

    /**
     * Resolves a {@link ReturnTargetCardsFromGraveyardToHandEffect} by returning each pre-targeted
     * card from the controller's graveyard to their hand. Silently skips cards that are no longer
     * in the graveyard at resolution time.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the return-to-hand effect configuration
     */

    public void processTargetedGraveyardCards(GameData gameData, StackEntry entry,
                                                BiConsumer<List<Card>, Card> cardConsumer,
                                                String logVerbPhrase, String logSuffix) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Card> movedCards = new ArrayList<>();

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                    cardConsumer.accept(graveyard, card);
                    movedCards.add(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!movedCards.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + logVerbPhrase);
            appendCardList(builder, movedCards);
            builder.text(logSuffix);
            gameBroadcastService.logAndBroadcast(gameData, builder.build());
            log.info("Game {} - {} moved {} card(s) from graveyard", gameData.id, playerName, movedCards.size());
        }
    }

    public void moveCardToDestination(GameData gameData, UUID playerId, Card card,
                                       GraveyardChoiceDestination destination,
                                       CardColor grantColor, CardSubtype grantSubtype,
                                       boolean enterTapped) {
        String playerName = gameData.playerIdToName.get(playerId);
        if (destination == GraveyardChoiceDestination.HAND) {
            gameData.addCardToHand(playerId, card);
            String logEntry = playerName + " returns " + card.getName() + " from graveyard to hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " returns " , card, " from graveyard to hand."));
        } else if (destination == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY) {
            gameData.playerDecks.get(playerId).addFirst(card);
            String logEntry = playerName + " puts " + card.getName() + " on top of their library from a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " puts " , card, " on top of their library from a graveyard."));
        } else if (destination == GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY) {
            gameData.playerDecks.get(playerId).addLast(card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " puts " , card, " on the bottom of their library from a graveyard."));
        } else {
            putCardOntoBattlefield(gameData, playerId, card, grantColor, grantSubtype, enterTapped);
        }
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card) {
        putCardOntoBattlefield(gameData, controllerId, card, null, null);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype) {
        putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype, false);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype, boolean enterTapped) {
        putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype, enterTapped, false);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking) {
        putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, null);
    }

    /**
     * Puts a card from a graveyard onto the battlefield, optionally with one counter of
     * {@code enterWithCounter} on it as it enters (e.g. Pyrrhic Revival's -1/-1 counter).
     */
    public Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter) {
        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield.
        // The card stays in the graveyard it was being returned from (the caller already removed it).
        if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameData.playerGraveyards.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " can't put ", card,
                    " onto the battlefield from a graveyard; it stays in the graveyard."));
            log.info("Game {} - {} blocked from entering the battlefield from a graveyard", gameData.id, card.getName());
            return null;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        applyPermanentGrants(permanent, grantColor, grantSubtype);
        if (enterWithCounter != null) {
            permanent.setCounterCount(enterWithCounter, 1);
        }
        if (enterTapped) {
            permanent.tap();
        }
        permanent.setEnteredFromGraveyardOwnerId(controllerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
        if (enterAttacking) {
            permanent.setAttacking(true);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String stateText = (enterTapped && enterAttacking) ? " tapped and attacking"
                : enterTapped ? " tapped"
                : enterAttacking ? " attacking"
                : "";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " puts ", card,
                " onto the battlefield" + stateText + " from a graveyard."));

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
        return permanent;
    }

    /**
     * Reanimates a targeted graveyard {@code card} under {@code controllerId}: removes it from
     * whichever graveyard it is in and puts it onto the battlefield. Returns the created permanent,
     * or {@code null} if it was blocked from entering (e.g. Grafdigger's Cage). Used by reanimation
     * Auras (Animate Dead) that must attach themselves to the returned creature.
     */
    public Permanent reanimateTargetedCard(GameData gameData, UUID controllerId, Card card) {
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        return putCardOntoBattlefield(gameData, controllerId, card, null, null, false, false, null);
    }

    /**
     * Destroys each targeted permanent and returns each card actually put into a graveyard this way
     * to the battlefield under the controller's control. Indestructible or regenerated permanents are
     * not returned.
     */

    /**
     * Resolves an {@link UndyingReturnEffect} (CR 702.93) by returning the dying card from its owner's
     * graveyard to the battlefield under its owner's control with a +1/+1 counter on it. The entry is
     * flagged as entering from the owner's graveyard so "enters from your graveyard" triggers (e.g.
     * Flayer of the Hatebound) fire. Fizzles silently if the card is no longer in a graveyard.
     */

    /**
     * Returns {@code true} if the given card is currently barred from entering the battlefield from
     * {@code zone} (e.g. a creature card while Grafdigger's Cage is on the battlefield).
     */
    public boolean isCardBlockedFromEnteringFromZone(GameData gameData, Card card, Zone zone) {
        return gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, zone);
    }

    public void applyPermanentGrants(Permanent permanent, CardColor grantColor, CardSubtype grantSubtype) {
        if (grantColor != null) {
            permanent.getGrantedColors().add(grantColor);
        }
        if (grantSubtype != null && !permanent.getGrantedSubtypes().contains(grantSubtype)) {
            permanent.getGrantedSubtypes().add(grantSubtype);
        }
    }

    public void putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                         boolean grantHaste, boolean exileAtEndStep) {
        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield.
        if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameData.playerGraveyards.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " can't put ", card,
                    " onto the battlefield from a graveyard; it stays in the graveyard."));
            log.info("Game {} - {} blocked from entering the battlefield from a graveyard", gameData.id, card.getName());
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        if (grantHaste) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
        }
        permanent.setEnteredFromGraveyardOwnerId(controllerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
        if (exileAtEndStep) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String hasteText = grantHaste ? " with haste" : "";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " returns ", card,
                " to the battlefield" + hasteText + "."));

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }

    public boolean exileCardFromAnyGraveyard(GameData gameData, UUID cardId, Card card) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                graveyardService.notifyCardsLeftGraveyard(gameData, pid);
                exileService.exileCard(gameData, pid, card);
                return true;
            }
        }
        return false;
    }

    public void handleCreatureEtbAndLegendRule(GameData gameData, UUID controllerId, Permanent permanent, Card card) {
        if (gameQueryService.isCreature(gameData, permanent)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    public void applyLifeGainEqualToManaValue(GameData gameData, UUID controllerId, Card card) {
        int manaValue = card.getManaValue();
        if (manaValue > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, manaValue);
        }
    }


    /**
     * Records that a permanent entered the battlefield under a non-owner's control (e.g. stolen
     * from an opponent's graveyard): stamps the ownership record and creates the indefinite
     * floating control effect that keeps it with {@code controllerId}.
     */
    public void trackStolenCreature(GameData gameData, UUID permanentId, UUID controllerId, UUID originalOwnerId) {
        gameData.stolenCreatures.put(permanentId, originalOwnerId);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), null, null, controllerId,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                permanentId, null, null, EffectDuration.PERMANENT, 0));
    }

    public record StolenCreatureResult(Permanent permanent, Card card, UUID originalOwnerId) {}

    public StolenCreatureResult stealFromOpponentGraveyard(GameData gameData, StackEntry entry, UUID controllerId) {
        // SPELL-slot casts (Gruesome Encore) carry the target on targetId; ETB triggers
        // (Puppeteer Clique) choose it via a multi-graveyard choice, landing it on targetCardIds.
        UUID targetId = entry.getTargetId() != null ? entry.getTargetId()
                : (entry.getTargetCardIds().isEmpty() ? null : entry.getTargetCardIds().getFirst());
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in graveyard)."));
            return null;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null || graveyardOwnerId.equals(controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target not in opponent's graveyard)."));
            return null;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
        Permanent permanent = new Permanent(targetCard);
        return new StolenCreatureResult(permanent, targetCard, graveyardOwnerId);
    }

    /**
     * Resolves a {@link PutCardFromOpponentGraveyardOntoBattlefieldEffect} by stealing a targeted
     * creature from an opponent's graveyard and putting it onto the battlefield under the controller's
     * control. Optionally enters tapped. If the stack entry has a positive X value, the opponent
     * also mills that many cards. Fizzles if the target is no longer in an opponent's graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (X value determines mill count)
     * @param effect   the steal-from-opponent effect configuration
     */

    /**
     * Resolves a {@link PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect} by stealing
     * a targeted creature from an opponent's graveyard and putting it onto the battlefield with haste.
     * The creature is exiled at the next end step. Fizzles if the target is no longer in an
     * opponent's graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     */

    /**
     * Resolves an {@link ExileCardsFromGraveyardEffect} by exiling each pre-targeted card that is
     * still in a graveyard. After exiling, the controller gains life if the effect specifies a
     * positive life gain amount.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the exile effect configuration (includes life gain amount)
     */

    /**
     * Resolves an {@link ExileTargetCardFromGraveyardAndImprintOnSourceEffect} by exiling a targeted
     * card from a graveyard and tracking it as imprinted on the source permanent. Validates the card
     * still matches the required type (if any). Fizzles if the target is no longer in a graveyard or
     * no longer matches the type requirement.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (source permanent receives the imprint)
     * @param effect   the imprint effect configuration (includes optional type requirement)
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling a targeted card from a
     * graveyard. Unlike the imprint variant, this does NOT track the exiled card on the source
     * permanent. Validates the card still matches the required type (if any). Fizzles if the target
     * is no longer in a graveyard or no longer matches the type requirement.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the exile effect configuration (includes optional type requirement)
     */

    /**
     * Exiles a targeted graveyard card, then creates a token copy of that card with optional
     * additional subtypes, haste, and end-step exile scheduling.
     */

    public void createTokenCopyFromCard(GameData gameData, StackEntry entry, Card sourceCard,
                                         List<CardSubtype> additionalSubtypes, boolean grantHaste,
                                         boolean exileAtEndStep) {
        createTokenCopyFromCard(gameData, entry, sourceCard, additionalSubtypes, grantHaste,
                exileAtEndStep, null, null, null);
    }

    /**
     * Variant that applies an Eternalize-style "except it's a 4/4 black Zombie" transformation to the
     * token copy: {@code colorOverride} (if non-null) replaces the copy's color and
     * {@code powerOverride}/{@code toughnessOverride} (if non-null) set its base P/T. The additional
     * subtypes are still added "in addition to" the copy's other types. Used by Hour of Eternity.
     */
    public void createTokenCopyFromCard(GameData gameData, StackEntry entry, Card sourceCard,
                                         List<CardSubtype> additionalSubtypes, boolean grantHaste,
                                         boolean exileAtEndStep, CardColor colorOverride,
                                         Integer powerOverride, Integer toughnessOverride) {
        UUID controllerId = entry.getControllerId();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = new Card();
            tokenCard.setName(sourceCard.getName());
            tokenCard.setType(sourceCard.getType());
            tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
            tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
            tokenCard.setToken(true);
            if (colorOverride != null) {
                tokenCard.setColor(colorOverride);
                tokenCard.setColors(List.of(colorOverride));
            } else {
                tokenCard.setColor(sourceCard.getColor());
            }
            tokenCard.setSupertypes(sourceCard.getSupertypes());
            tokenCard.setPower(powerOverride != null ? powerOverride : sourceCard.getPower());
            tokenCard.setToughness(toughnessOverride != null ? toughnessOverride : sourceCard.getToughness());
            tokenCard.setCardText(sourceCard.getCardText());
            tokenCard.setSetCode(sourceCard.getSetCode());
            tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

            List<CardSubtype> subtypes = new ArrayList<>();
            if (sourceCard.getSubtypes() != null) {
                subtypes.addAll(sourceCard.getSubtypes());
            }
            if (additionalSubtypes != null) {
                for (CardSubtype subtype : additionalSubtypes) {
                    if (!subtypes.contains(subtype)) {
                        subtypes.add(subtype);
                    }
                }
            }
            tokenCard.setSubtypes(subtypes);

            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            if (sourceCard.getKeywords() != null) {
                keywords.addAll(sourceCard.getKeywords());
            }
            if (grantHaste) {
                keywords.add(Keyword.HASTE);
            }
            tokenCard.setKeywords(keywords);

            for (EffectSlot slot : EffectSlot.values()) {
                for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
                    tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                }
            }
            for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
                tokenCard.addActivatedAbility(ability);
            }
            tokenCard.copyTargetingFrom(sourceCard);

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent);

            if (exileAtEndStep) {
                gameData.queueDelayedAction(new DelayedPermanentAction(tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
            }

            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("A token copy of ", sourceCard,
                    grantHaste ? " is created with haste." : " is created."));
            log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(),
                    entry.getCard().getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
        }
    }

    /**
     * Resolves an {@link ExileGraveyardCardWithConditionalBonusEffect} by exiling the targeted card
     * from a graveyard, then conditionally gaining life (if the exiled card is a creature) or
     * boosting the source permanent (if the exiled card is a noncreature).
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the effect configuration (life gain amount, power/toughness boost amounts)
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling
     * the pre-targeted cards from an opponent's graveyard. Reads target card IDs from
     * {@code entry.getTargetCardIds()}. Cards that are no longer in a graveyard at
     * resolution time are silently skipped.
     */

    /**
     * Resolves an {@link ExileCreaturesFromGraveyardAndCreateTokensEffect} by exiling each pre-targeted
     * creature card from graveyards and creating a 2/2 black Zombie creature token for each card
     * successfully exiled.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling all cards in the target
     * player's graveyard. Does nothing beyond logging if the graveyard is already empty.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (target player ID is in targetId)
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling all cards from every
     * opponent's graveyard. Does not affect the controller's graveyard.
     */

    /**
     * Resolves a {@link ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect} by exiling all
     * non-basic-land cards from the target player's graveyard, then searching that player's library
     * for all cards with the same name as any card exiled this way and exiling them too.
     * Finally, the target player shuffles their library.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (target player ID is in targetId)
     */

    /**
     * Resolves a {@link PutImprintedCreatureOntoBattlefieldEffect} by revealing the card imprinted
     * on the source and, if it is a creature card, putting it onto the battlefield under the
     * controller's control. Non-creature cards remain in exile.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (card must have an imprinted card)
     */

    /**
     * Resolves a {@link PutImprintedCardIntoOwnersHandEffect} by putting the card imprinted
     * on the source into its owner's hand. The owner is determined by which player's exile zone
     * contains the card. Used by cards like Hoarding Dragon.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (card must have an imprinted card)
     */

    /**
     * Resolves a {@link ReturnDyingCreatureToBattlefieldAndAttachSourceEffect} by returning a
     * creature that just died back to the battlefield and attaching the source equipment to it.
     * Used by equipment with triggered abilities like Nim Deathmantle. Fizzles if the dying
     * card is no longer in a graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (targetId is the source equipment)
     * @param effect   the return-and-attach effect (dyingCardId identifies the creature to return)
     */

    /**
     * Resolves a {@link CastTargetInstantOrSorceryFromGraveyardEffect} by
     * validating the targeted instant or sorcery card is still in a graveyard matching the scope,
     * then queuing a may-cast choice for the controller.
     */




    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by forcing the affected
     * player to exile cards from their own graveyard.
     * <ul>
     *   <li>0 cards in graveyard: nothing happens</li>
     *   <li>1 to count cards: all are auto-exiled</li>
     *   <li>More than count cards: the player chooses which to exile</li>
     * </ul>
     */

    /**
     * Resolves a {@link TargetPlayerExilesCardFromGraveyardEffect} by forcing the target player
     * to exile a card from their graveyard. If the graveyard has only one card, it is auto-exiled.
     * If the exiled card is a creature and {@code lifeGainIfCreature > 0}, the ability's controller
     * gains that much life.
     */

    /**
     * Sets up a graveyard choice interaction for the player to choose a card to exile.
     * If multiple exiles are needed, the remaining count is tracked so subsequent choices
     * are presented after each exile.
     */
    public void beginGraveyardExileChoice(GameData gameData, UUID playerId, int remainingCount) {
        beginGraveyardExileChoice(gameData, playerId, remainingCount, null);
    }

    /**
     * Variant of {@link #beginGraveyardExileChoice(GameData, UUID, int)} that excludes a specific
     * card from the choice (by identity). Used when a spell resolving with a "you may exile a card
     * from your graveyard" clause has already been placed into its owner's graveyard — that spell
     * card is not actually in the graveyard yet per the rules and must not be a valid choice.
     */
    public void beginGraveyardExileChoice(GameData gameData, UUID playerId, int remainingCount, Card excludedCard) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> validIndices = IntStream.range(0, graveyard.size())
                .filter(i -> excludedCard == null || graveyard.get(i) != excludedCard)
                .boxed().toList();

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(playerId, validIndices, GraveyardChoiceDestination.EXILE,
                        "Choose a card to exile from your graveyard.")
                .exileRemainingCount(remainingCount)
                .build());
    }

    /**
     * Resolves an {@link EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect}. Each player
     * returns up to {@code maxCount} matching cards from their graveyard to the battlefield.
     * If a player has fewer matching cards than {@code maxCount}, all are returned automatically.
     * If a player has more, they are prompted to choose via the graveyard choice queue.
     */

    /**
     * Resolves a {@link ReturnOneOfEachSubtypeFromGraveyardToHandEffect} by queuing sequential
     * graveyard choices — one per subtype. For each subtype that has matching cards in the
     * controller's graveyard, a choice entry is queued. If only one card matches a subtype,
     * it is returned automatically without prompting. Cards already returned for a previous
     * subtype are naturally excluded because they have been removed from the graveyard.
     */

    /**
     * Pops the next entry from the graveyard return queue and prompts that player to choose
     * a card to return from their graveyard. Does nothing if the queue is empty.
     */
    public void beginNextGraveyardReturnFromQueue(GameData gameData) {
        if (gameData.pendingGraveyardReturnQueue.isEmpty()) {
            return;
        }

        PendingGraveyardReturnChoice next = gameData.pendingGraveyardReturnQueue.removeFirst();
        List<Card> graveyard = gameData.playerGraveyards.get(next.playerId());
        if (graveyard == null || graveyard.isEmpty()) {
            // Player's graveyard is empty — skip to next
            beginNextGraveyardReturnFromQueue(gameData);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), next.filter(), null)) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            // No matching cards left — skip to next
            beginNextGraveyardReturnFromQueue(gameData);
            return;
        }

        // Re-enqueue with decremented remaining count if the player has more picks after this one
        if (next.remainingCount() > 1) {
            gameData.pendingGraveyardReturnQueue.addFirst(
                    new PendingGraveyardReturnChoice(next.playerId(), next.remainingCount() - 1, next.filter(),
                            next.destination(), next.skipRemainingOnDecline()));
        }

        GraveyardChoiceDestination destination = next.destination();
        String filterLabel = CardPredicateUtils.describeFilter(next.filter());
        String destText = destination == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield";
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(next.playerId(), matchingIndices, destination,
                        "Return a " + filterLabel + " from your graveyard to " + destText + ".")
                .build());
    }

    /**
     * Registers a delayed trigger that will return the specified card from its owner's graveyard
     * to their hand at the beginning of the next end step. The card must still be in the graveyard
     * when the end step arrives for the return to happen.
     *
     * <p>Used by Tiana, Ship's Caretaker.
     */

    /**
     * Registers a delayed trigger that will return the source card from its owner's graveyard
     * to the battlefield transformed at the beginning of the next end step.
     */



    /**
     * Step 1: opponent has assigned cards to Pile 1. Unselected cards form Pile 2.
     * Prompt controller to choose which pile to put onto the battlefield.
     */
    public void completeCardPileSeparationStep1(GameData gameData, List<UUID> pile1CardIds) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);

        List<UUID> pile1 = new ArrayList<>(state.pile1Ids());
        pile1.addAll(pile1CardIds);
        // Pile 2 is everything not in Pile 1
        List<UUID> pile2 = new ArrayList<>(state.pile2Ids());
        for (Card card : state.cards()) {
            if (!pile1CardIds.contains(card.getId())) {
                pile2.add(card.getId());
            }
        }

        // Re-queue with the piles filled — step 2 (the pile-choice may prompt) polls it. Preserve the
        // disposition so BATTLEFIELD (Boneyard Parley) and HAND (Unesh) both survive the re-queue.
        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), state.targetPlayerId(),
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2, state.disposition()));

        String pile1Desc = buildCardPileDescription(state.cards(), pile1);
        String pile2Desc = buildCardPileDescription(state.cards(), pile2);

        UUID opponentId = state.targetPlayerId();
        String opponentName = gameData.playerIdToName.get(opponentId);
        GameLog.Builder pileLog = GameLog.builder().text(opponentName + " separates cards into two piles. Pile 1: ");
        appendCardPile(pileLog, state.cards(), pile1);
        pileLog.text(". Pile 2: ");
        appendCardPile(pileLog, state.cards(), pile2);
        pileLog.text(".");
        gameBroadcastService.logAndBroadcast(gameData, pileLog.build());

        UUID controllerId = state.controllerId();
        String destText = state.disposition() == CardPileDisposition.HAND ? "put into your hand" : "put onto the battlefield";
        String prompt = "Choose a pile to " + destText + ". Yes = Pile 1 (" + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, controllerId, List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Step 2: controller has chosen a pile. Put chosen pile onto the battlefield under controller's control;
     * return the other pile to their owners' graveyards.
     */
    public void completeCardPileSeparationStep2(GameData gameData, boolean accepted) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        List<UUID> chosenPileCardIds = accepted
                ? new ArrayList<>(state.pile1Ids())
                : new ArrayList<>(state.pile2Ids());
        List<UUID> otherPileCardIds = accepted
                ? new ArrayList<>(state.pile2Ids())
                : new ArrayList<>(state.pile1Ids());
        String chosenPileName = accepted ? "Pile 1" : "Pile 2";

        UUID controllerId = state.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        List<Card> allCards = new ArrayList<>(state.cards());
        Map<UUID, UUID> cardOwners = new HashMap<>(state.cardOwners());

        String chosenDesc = buildCardPileDescription(allCards, chosenPileCardIds);
        String otherDesc = buildCardPileDescription(allCards, otherPileCardIds);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " chooses " + chosenPileName + "."));

        if (state.disposition() == CardPileDisposition.HAND) {
            // Fact-or-Fiction (Unesh): chosen pile → controller's hand; other pile → controller's graveyard.
            for (UUID cardId : chosenPileCardIds) {
                Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null) {
                    gameData.addCardToHand(controllerId, card);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(controllerName + " puts ", card, " into their hand."));
                }
            }
            for (UUID cardId : otherPileCardIds) {
                Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null) {
                    gameData.playerGraveyards.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(controllerName + " puts ", card, " into their graveyard."));
                }
            }
            return;
        }

        // Chosen pile → battlefield under controller's control
        for (UUID cardId : chosenPileCardIds) {
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (card != null) {
                putCardOntoBattlefieldFromExile(gameData, controllerId, card);
            }
        }

        // Other pile → owners' graveyards
        for (UUID cardId : otherPileCardIds) {
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (card != null) {
                UUID ownerId = cardOwners.get(cardId);
                gameData.playerGraveyards.get(ownerId).add(card);
                String ownerName = gameData.playerIdToName.get(ownerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " returns to " + ownerName + "'s graveyard."));
            }
        }
    }

    public void putCardOntoBattlefieldFromExile(GameData gameData, UUID controllerId, Card card) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " puts " , card, " onto the battlefield."));

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }

    private String buildCardPileDescription(List<Card> allCards, List<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID cardId : cardIds) {
            allCards.stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .ifPresent(c -> names.add(c.getName()));
        }
        return String.join(", ", names);
    }

    private void appendCardPile(GameLog.Builder builder, List<Card> allCards, List<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            builder.text("empty");
            return;
        }
        List<Card> pileCards = new ArrayList<>();
        for (UUID cardId : cardIds) {
            allCards.stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .ifPresent(pileCards::add);
        }
        appendCardList(builder, pileCards);
    }
}
