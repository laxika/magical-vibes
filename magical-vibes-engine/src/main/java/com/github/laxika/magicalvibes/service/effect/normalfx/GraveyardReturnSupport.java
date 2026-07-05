package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
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
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromOwnGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardsFromOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllOpponentsGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCardIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfEachSubtypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToTargetsThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.UndyingReturnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import com.github.laxika.magicalvibes.model.CounterType;

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
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
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
                gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
            gameData.interaction.setPendingAuraCard(targetCard);

            playerInputService.beginPermanentChoice(gameData, controllerId, attachTargetIds,
                    "Choose a creature you control to attach " + targetCard.getName() + " to.");
            return;
        }

        // For TOP_OF_OWNERS_LIBRARY, find the graveyard owner before removal
        UUID destinationPlayerId = controllerId;
        if (effect.destination() == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY) {
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

        if (effect.gainLifeEqualToManaValue()) {
            applyLifeGainEqualToManaValue(gameData, controllerId, targetCard);
        }
    }

    public void resolveReturnAll(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                  UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (effect.thisTurnOnly() || effect.fromAnywhereThisTurn()) {
            Set<UUID> trackedIds = effect.fromAnywhereThisTurn()
                    ? gameData.cardsPutIntoGraveyardFromAnywhereThisTurn.getOrDefault(controllerId, Set.of())
                    : gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, Set.of());
            String sourceLabel = effect.fromAnywhereThisTurn() ? "from anywhere" : "from the battlefield";

            if (graveyard == null || graveyard.isEmpty() || trackedIds.isEmpty()) {
                String logEntry = entry.getDescription() + " - no creature cards were put into your graveyard " + sourceLabel + " this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                String logEntry = entry.getDescription() + " - no creature cards were put into your graveyard " + sourceLabel + " this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }

            List<String> returnedNames = new ArrayList<>();
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : toReturn) {
                    graveyard.remove(card);
                    gameData.addCardToHand(controllerId, card);
                    returnedNames.add(card.getName());
                    trackedIds.remove(card.getId());
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " returns " + String.join(", ", returnedNames)
                    + " from graveyard to hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returns {} creature card(s) from graveyard to hand",
                    gameData.id, playerName, returnedNames.size());
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

        List<String> returnedNames = new ArrayList<>();
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
                    UUID targetPlayerId = effect.underOwnersControl() ? gyEntry.getKey() : controllerId;
                    if (effect.destination() == GraveyardChoiceDestination.HAND) {
                        gameData.addCardToHand(targetPlayerId, card);
                    } else {
                        putCardOntoBattlefield(gameData, targetPlayerId, card, effect.grantColor(), effect.grantSubtype(),
                                effect.enterTapped(), effect.enterAttacking());
                    }
                    returnedNames.add(card.getName());
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (returnedNames.isEmpty()) {
            return;
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destName = effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        String logEntry = playerName + " puts " + String.join(", ", returnedNames)
                + " onto " + destName + " from " + (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS ? "all graveyards" : "graveyard") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} onto {} from graveyards", gameData.id, playerName,
                String.join(", ", returnedNames), destName);
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
                String exileLog = playerName + " exiles " + sourceCard.getName() + " from graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, exileLog);
                log.info("Game {} - {} exiles {} from graveyard", gameData.id, playerName, sourceCard.getName());
            }
        }

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int count = Math.min(effect.randomCount(), matchingCards.size());
        List<String> returnedNames = new ArrayList<>();
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
                returnedNames.add(randomCard.getName());
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        String logEntry = playerName + " returns " + String.join(", ", returnedNames) + " at random from graveyard to " + destText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returns {} at random from graveyard to {}",
                gameData.id, playerName, String.join(", ", returnedNames), destText);
    }

    public void resolveFromControllersGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                                 UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield";
        String prompt = "Return a " + filterLabel + " from your graveyard to " + destText + ".";

        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, effect.destination(), prompt)
                .gainLifeEqualToManaValue(effect.gainLifeEqualToManaValue());
        if (effect.grantColor() != null) {
            choice.grantColor(effect.grantColor());
        }
        if (effect.grantSubtype() != null) {
            choice.grantSubtype(effect.grantSubtype());
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                                                Function<List<String>, String> logSuffix) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<String> movedNames = new ArrayList<>();

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                    cardConsumer.accept(graveyard, card);
                    movedNames.add(card.getName());
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!movedNames.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + logSuffix.apply(movedNames));
            log.info("Game {} - {} moved {} card(s) from graveyard", gameData.id, playerName, movedNames.size());
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (destination == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY) {
            gameData.playerDecks.get(playerId).addFirst(card);
            String logEntry = playerName + " puts " + card.getName() + " on top of their library from a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield.
        // The card stays in the graveyard it was being returned from (the caller already removed it).
        if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameData.playerGraveyards.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
            String blockedLog = gameData.playerIdToName.get(controllerId) + " can't put " + card.getName()
                    + " onto the battlefield from a graveyard; it stays in the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, blockedLog);
            log.info("Game {} - {} blocked from entering the battlefield from a graveyard", gameData.id, card.getName());
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        applyPermanentGrants(permanent, grantColor, grantSubtype);
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
        String logEntry = playerName + " puts " + card.getName() + " onto the battlefield" + stateText + " from a graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
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
            String blockedLog = gameData.playerIdToName.get(controllerId) + " can't put " + card.getName()
                    + " onto the battlefield from a graveyard; it stays in the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, blockedLog);
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
            gameData.pendingTokenExilesAtEndStep.add(permanent.getId());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String hasteText = grantHaste ? " with haste" : "";
        String logEntry = playerName + " returns " + card.getName() + " to the battlefield" + hasteText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

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


    public void trackStolenCreature(GameData gameData, UUID permanentId, UUID originalOwnerId) {
        gameData.stolenCreatures.put(permanentId, originalOwnerId);
        gameData.permanentControlStolenCreatures.add(permanentId);
    }

    public record StolenCreatureResult(Permanent permanent, Card card, UUID originalOwnerId) {}

    public StolenCreatureResult stealFromOpponentGraveyard(GameData gameData, StackEntry entry, UUID controllerId) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target no longer in graveyard).");
            return null;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null || graveyardOwnerId.equals(controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target not in opponent's graveyard).");
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
     * Resolves an {@link ExileTargetCardFromGraveyardEffect} by exiling a targeted card from a
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
        UUID controllerId = entry.getControllerId();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = new Card();
            tokenCard.setName(sourceCard.getName());
            tokenCard.setType(sourceCard.getType());
            tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
            tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
            tokenCard.setToken(true);
            tokenCard.setColor(sourceCard.getColor());
            tokenCard.setSupertypes(sourceCard.getSupertypes());
            tokenCard.setPower(sourceCard.getPower());
            tokenCard.setToughness(sourceCard.getToughness());
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
                gameData.pendingTokenExilesAtEndStep.add(tokenPermanent.getId());
            }

            String logMsg = grantHaste
                    ? "A token copy of " + sourceCard.getName() + " is created with haste."
                    : "A token copy of " + sourceCard.getName() + " is created.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
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
     * Resolves an {@link ExileTargetCardsFromOpponentGraveyardEffect} by exiling
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
     * Resolves an {@link ExileTargetPlayerGraveyardEffect} by exiling all cards in the target
     * player's graveyard. Does nothing beyond logging if the graveyard is already empty.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (target player ID is in targetId)
     */

    /**
     * Resolves an {@link ExileAllOpponentsGraveyardsEffect} by exiling all cards from every
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
     * Resolves an {@link ExileCardsFromOwnGraveyardEffect} by forcing the affected
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

        // Re-queue with the piles filled — step 2 (the pile-choice may prompt) polls it.
        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), state.targetPlayerId(),
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2));

        String pile1Desc = buildCardPileDescription(state.cards(), pile1);
        String pile2Desc = buildCardPileDescription(state.cards(), pile2);

        UUID opponentId = state.targetPlayerId();
        String opponentName = gameData.playerIdToName.get(opponentId);
        gameBroadcastService.logAndBroadcast(gameData,
                opponentName + " separates cards into two piles. Pile 1: " + pile1Desc + ". Pile 2: " + pile2Desc + ".");

        UUID controllerId = state.controllerId();
        String prompt = "Choose a pile to put onto the battlefield. Yes = Pile 1 (" + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
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

        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " chooses " + chosenPileName + ".");

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
                gameBroadcastService.logAndBroadcast(gameData,
                        card.getName() + " returns to " + ownerName + "'s graveyard.");
            }
        }
    }

    public void putCardOntoBattlefieldFromExile(GameData gameData, UUID controllerId, Card card) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " puts " + card.getName() + " onto the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

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
}
