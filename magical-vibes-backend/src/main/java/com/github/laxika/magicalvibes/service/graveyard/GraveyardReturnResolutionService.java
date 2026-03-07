package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Resolves all graveyard-related effects during stack resolution. Handles returning cards from
 * graveyards to hand or battlefield, exiling cards from graveyards, stealing creatures from
 * opponent graveyards, imprint mechanics, and creating tokens from exiled graveyard creatures.
 *
 * <p>Each handler method is registered via {@link HandlesEffect} and dispatched automatically
 * by the effect resolution framework.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardReturnResolutionService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

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
    @HandlesEffect(ReturnCardFromGraveyardEffect.class)
    void resolveReturnCardFromGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard().getId();

        // Case 1: Pre-targeted (from spell cast or activated ability targeting)
        if (entry.getTargetZone() == Zone.GRAVEYARD && entry.getTargetPermanentId() != null) {
            resolvePreTargeted(gameData, entry, effect, controllerId, sourceCardId);
            return;
        }

        // Case 2: Return all matching cards (no choice)
        if (effect.returnAll()) {
            resolveReturnAll(gameData, entry, effect, controllerId, sourceCardId);
            return;
        }

        // Case 3: Search and choose at resolution
        if (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            resolveFromAllGraveyards(gameData, entry, effect, controllerId, sourceCardId);
        } else {
            resolveFromControllersGraveyard(gameData, entry, effect, controllerId, sourceCardId);
        }
    }

    private void resolvePreTargeted(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                    UUID controllerId, UUID sourceCardId) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (targetCard == null || (effect.filter() != null && !gameQueryService.matchesCardPredicate(targetCard, effect.filter(), sourceCardId))) {
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
                    if (gameQueryService.matchesPermanentPredicate(gameData, p, effect.attachmentTarget())) {
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
        moveCardToDestination(gameData, destinationPlayerId, targetCard, effect.destination());

        if (effect.gainLifeEqualToManaValue()) {
            applyLifeGainEqualToManaValue(gameData, controllerId, targetCard);
        }
    }

    private void resolveReturnAll(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                  UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (effect.thisTurnOnly()) {
            Set<UUID> trackedIds = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                    .getOrDefault(controllerId, Set.of());

            if (graveyard == null || graveyard.isEmpty() || trackedIds.isEmpty()) {
                String logEntry = entry.getDescription() + " - no creature cards were put into your graveyard from the battlefield this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }

            List<Card> toReturn = new ArrayList<>();
            for (Card card : graveyard) {
                boolean isCreatureCard = card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE);
                if (!card.isToken() && isCreatureCard && trackedIds.contains(card.getId())) {
                    toReturn.add(card);
                }
            }

            if (toReturn.isEmpty()) {
                String logEntry = entry.getDescription() + " - no creature cards were put into your graveyard from the battlefield this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }

            List<Card> hand = gameData.playerHands.get(controllerId);
            List<String> returnedNames = new ArrayList<>();
            for (Card card : toReturn) {
                graveyard.remove(card);
                hand.add(card);
                returnedNames.add(card.getName());
                trackedIds.remove(card.getId());
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " returns " + String.join(", ", returnedNames)
                    + " from graveyard to hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returns {} creature card(s) from graveyard to hand",
                    gameData.id, playerName, returnedNames.size());
            return;
        }

        // Return all matching (e.g. self-return)
        if (graveyard == null) {
            return;
        }

        List<Card> toReturn = new ArrayList<>();
        for (Card card : graveyard) {
            if (gameQueryService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                toReturn.add(card);
            }
        }

        if (toReturn.isEmpty()) {
            return;
        }

        List<String> returnedNames = new ArrayList<>();
        for (Card card : toReturn) {
            graveyard.remove(card);
            if (effect.destination() == GraveyardChoiceDestination.HAND) {
                gameData.playerHands.get(controllerId).add(card);
            } else {
                putCardOntoBattlefield(gameData, controllerId, card);
            }
            returnedNames.add(card.getName());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destName = effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        String logEntry = playerName + " returns " + String.join(", ", returnedNames)
                + " from graveyard to " + destName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returns {} from graveyard to {}", gameData.id, playerName,
                String.join(", ", returnedNames), destName);
    }

    private void resolveFromControllersGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
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
            if (gameQueryService.matchesCardPredicate(graveyard.get(i), effect.filter(), sourceCardId)) {
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

        gameData.interaction.prepareGraveyardChoice(effect.destination(), null);
        gameData.interaction.setGraveyardChoiceGainLifeEqualToManaValue(effect.gainLifeEqualToManaValue());

        if (effect.attachToSource()) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(sourceCardId)) {
                        gameData.interaction.setGraveyardChoiceAttachToSourcePermanentId(p.getId());
                        break;
                    }
                }
            }
        }

        playerInputService.beginGraveyardChoice(gameData, controllerId, matchingIndices, prompt);
    }

    private void resolveFromAllGraveyards(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                          UUID controllerId, UUID sourceCardId) {
        List<Card> cardPool = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (gameQueryService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
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

        gameData.interaction.prepareGraveyardChoice(effect.destination(), cardPool);
        playerInputService.beginGraveyardChoice(gameData, controllerId, indices, prompt);
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
    @HandlesEffect(PutTargetCardsFromGraveyardOnTopOfLibraryEffect.class)
    void resolvePutTargetCardsFromGraveyardOnTopOfLibrary(GameData gameData, StackEntry entry,
                                                          PutTargetCardsFromGraveyardOnTopOfLibraryEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> library.addFirst(card),
                movedNames -> " puts " + String.join(", ", movedNames) + " on top of their library from graveyard.");
    }

    /**
     * Resolves a {@link ReturnTargetCardsFromGraveyardToHandEffect} by returning each pre-targeted
     * card from the controller's graveyard to their hand. Silently skips cards that are no longer
     * in the graveyard at resolution time.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the return-to-hand effect configuration
     */
    @HandlesEffect(ReturnTargetCardsFromGraveyardToHandEffect.class)
    void resolveReturnTargetCardsFromGraveyardToHand(GameData gameData, StackEntry entry,
                                                     ReturnTargetCardsFromGraveyardToHandEffect effect) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> hand.add(card),
                movedNames -> " returns " + String.join(", ", movedNames) + " from graveyard to hand.");
    }

    private void processTargetedGraveyardCards(GameData gameData, StackEntry entry,
                                                BiConsumer<List<Card>, Card> cardConsumer,
                                                Function<List<String>, String> logSuffix) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<String> movedNames = new ArrayList<>();

        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null && graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                cardConsumer.accept(graveyard, card);
                movedNames.add(card.getName());
            }
        }

        if (!movedNames.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + logSuffix.apply(movedNames));
            log.info("Game {} - {} moved {} card(s) from graveyard", gameData.id, playerName, movedNames.size());
        }
    }

    private void moveCardToDestination(GameData gameData, UUID playerId, Card card,
                                       GraveyardChoiceDestination destination) {
        String playerName = gameData.playerIdToName.get(playerId);
        if (destination == GraveyardChoiceDestination.HAND) {
            gameData.playerHands.get(playerId).add(card);
            String logEntry = playerName + " returns " + card.getName() + " from graveyard to hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (destination == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY) {
            gameData.playerDecks.get(playerId).addFirst(card);
            String logEntry = playerName + " puts " + card.getName() + " on top of their library from a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            putCardOntoBattlefield(gameData, playerId, card);
        }
    }

    private void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " puts " + card.getName() + " onto the battlefield from a graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }

    private boolean exileCardFromAnyGraveyard(GameData gameData, UUID cardId, Card card) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                gameData.playerExiledCards.get(pid).add(card);
                return true;
            }
        }
        return false;
    }

    private void handleCreatureEtbAndLegendRule(GameData gameData, UUID controllerId, Permanent permanent, Card card) {
        if (gameQueryService.isCreature(gameData, permanent)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private void applyLifeGainEqualToManaValue(GameData gameData, UUID controllerId, Card card) {
        int manaValue = card.getManaValue();
        if (manaValue > 0) {
            applyLifeGain(gameData, controllerId, manaValue);
        }
    }

    private void applyLifeGain(GameData gameData, UUID controllerId, int amount) {
        String playerName = gameData.playerIdToName.get(controllerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }
        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);
        String logEntry = playerName + " gains " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);
    }

    private void trackStolenCreature(GameData gameData, UUID permanentId, UUID originalOwnerId) {
        gameData.stolenCreatures.put(permanentId, originalOwnerId);
        gameData.permanentControlStolenCreatures.add(permanentId);
    }

    private record StolenCreatureResult(Permanent permanent, Card card, UUID originalOwnerId) {}

    private StolenCreatureResult stealFromOpponentGraveyard(GameData gameData, StackEntry entry, UUID controllerId) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
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
    @HandlesEffect(PutCardFromOpponentGraveyardOntoBattlefieldEffect.class)
    void resolvePutFromOpponentGraveyardAndMill(GameData gameData, StackEntry entry,
            PutCardFromOpponentGraveyardOntoBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();

        StolenCreatureResult result = stealFromOpponentGraveyard(gameData, entry, controllerId);
        if (result == null) return;

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        if (effect.tapped()) {
            result.permanent().tap();
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, result.permanent(), enterTappedTypes);

        trackStolenCreature(gameData, result.permanent().getId(), result.originalOwnerId());

        String tappedText = effect.tapped() ? " tapped" : "";
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " puts " + result.card().getName() + " onto the battlefield" + tappedText + " under their control.");

        handleCreatureEtbAndLegendRule(gameData, controllerId, result.permanent(), result.card());

        if (xValue > 0) {
            List<Card> opponentDeck = gameData.playerDecks.get(result.originalOwnerId());
            List<Card> opponentGraveyard = gameData.playerGraveyards.get(result.originalOwnerId());
            int cardsToMill = Math.min(xValue, opponentDeck.size());
            List<String> milledNames = new ArrayList<>();
            for (int i = 0; i < cardsToMill; i++) {
                Card milled = opponentDeck.removeFirst();
                opponentGraveyard.add(milled);
                milledNames.add(milled.getName());
            }
            if (cardsToMill > 0) {
                String opponentName = gameData.playerIdToName.get(result.originalOwnerId());
                gameBroadcastService.logAndBroadcast(gameData,
                        opponentName + " mills " + cardsToMill + " cards (" + String.join(", ", milledNames) + ").");
            }
        }
    }

    /**
     * Resolves a {@link PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect} by stealing
     * a targeted creature from an opponent's graveyard and putting it onto the battlefield with haste.
     * The creature is exiled at the next end step. Fizzles if the target is no longer in an
     * opponent's graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     */
    @HandlesEffect(PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect.class)
    void resolvePutCreatureFromOpponentGraveyardWithExile(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        StolenCreatureResult result = stealFromOpponentGraveyard(gameData, entry, controllerId);
        if (result == null) return;

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        result.permanent().getGrantedKeywords().add(Keyword.HASTE);
        result.permanent().setExileIfLeavesBattlefield(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, result.permanent(), enterTappedTypes);

        trackStolenCreature(gameData, result.permanent().getId(), result.originalOwnerId());
        gameData.pendingTokenExilesAtEndStep.add(result.permanent().getId());

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " puts " + result.card().getName() + " onto the battlefield under their control with haste.");

        handleCreatureEtbAndLegendRule(gameData, controllerId, result.permanent(), result.card());
    }

    /**
     * Resolves an {@link ExileCardsFromGraveyardEffect} by exiling each pre-targeted card that is
     * still in a graveyard. After exiling, the controller gains life if the effect specifies a
     * positive life gain amount.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the exile effect configuration (includes life gain amount)
     */
    @HandlesEffect(ExileCardsFromGraveyardEffect.class)
    void resolveExileCardsFromGraveyard(GameData gameData, StackEntry entry, ExileCardsFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Exile targeted cards that are still in graveyards
        if (targetCardIds != null && !targetCardIds.isEmpty()) {
            List<String> exiledNames = new ArrayList<>();
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    exiledNames.add(card.getName());
                    exileCardFromAnyGraveyard(gameData, cardId, card);
                }
            }
            if (!exiledNames.isEmpty()) {
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled {} cards from graveyards", gameData.id, playerName, exiledNames.size());
            }
        }

        // Gain life after exile
        if (effect.lifeGain() > 0) {
            applyLifeGain(gameData, controllerId, effect.lifeGain());
        }
    }

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
    @HandlesEffect(ExileTargetCardFromGraveyardAndImprintOnSourceEffect.class)
    void resolveExileTargetCardAndImprintOnSource(GameData gameData, StackEntry entry,
                                                   ExileTargetCardFromGraveyardAndImprintOnSourceEffect effect) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getDescription() + " fizzles (target no longer in a graveyard).");
            return;
        }

        if (effect.requiredType() != null && targetCard.getType() != effect.requiredType()
                && !targetCard.getAdditionalTypes().contains(effect.requiredType())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getDescription() + " fizzles (target is no longer a valid "
                            + effect.requiredType().name().toLowerCase() + " card).");
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

        // Add to graveyard owner's exiled cards
        if (graveyardOwnerId != null) {
            gameData.playerExiledCards.computeIfAbsent(graveyardOwnerId, k -> new ArrayList<>()).add(targetCard);
        }

        // Track as imprinted on source permanent
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            gameData.permanentExiledCards
                    .computeIfAbsent(sourcePermanentId, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(targetCard);
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " exiles " + targetCard.getName() + " from a graveyard.");
    }

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
    @HandlesEffect(ExileTargetCardFromGraveyardEffect.class)
    void resolveExileTargetCardFromGraveyard(GameData gameData, StackEntry entry,
                                              ExileTargetCardFromGraveyardEffect effect) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getDescription() + " fizzles (target no longer in a graveyard).");
            return;
        }

        if (effect.requiredType() != null && targetCard.getType() != effect.requiredType()
                && !targetCard.getAdditionalTypes().contains(effect.requiredType())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getDescription() + " fizzles (target is no longer a valid "
                            + effect.requiredType().name().toLowerCase() + " card).");
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

        // Add to graveyard owner's exiled cards
        if (graveyardOwnerId != null) {
            gameData.playerExiledCards.computeIfAbsent(graveyardOwnerId, k -> new ArrayList<>()).add(targetCard);
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " exiles " + targetCard.getName() + " from a graveyard.");
    }

    /**
     * Resolves an {@link ExileCreaturesFromGraveyardAndCreateTokensEffect} by exiling each pre-targeted
     * creature card from graveyards and creating a 2/2 black Zombie creature token for each card
     * successfully exiled.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     */
    @HandlesEffect(ExileCreaturesFromGraveyardAndCreateTokensEffect.class)
    void resolveExileCreaturesAndCreateTokens(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        int tokensToCreate = 0;
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                exileCardFromAnyGraveyard(gameData, cardId, card);
                String exileLog = playerName + " exiles " + card.getName() + " from graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, exileLog);
                tokensToCreate++;
            }
        }

        for (int i = 0; i < tokensToCreate; i++) {
            Card tokenCard = new Card();
            tokenCard.setName("Zombie");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(CardColor.BLACK);
            tokenCard.setPower(2);
            tokenCard.setToughness(2);
            tokenCard.setSubtypes(List.of(CardSubtype.ZOMBIE));

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String tokenLog = "A 2/2 Zombie creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, tokenLog);

            handleCreatureEtbAndLegendRule(gameData, controllerId, tokenPermanent, tokenCard);

            log.info("Game {} - Zombie token created for player {}", gameData.id, controllerId);
        }
    }

    /**
     * Resolves an {@link ExileTargetPlayerGraveyardEffect} by exiling all cards in the target
     * player's graveyard. Does nothing beyond logging if the graveyard is already empty.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (target player ID is in targetPermanentId)
     */
    @HandlesEffect(ExileTargetPlayerGraveyardEffect.class)
    void resolveExileTargetPlayerGraveyard(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is already empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int count = graveyard.size();
        List<Card> exiledCards = gameData.playerExiledCards.get(targetPlayerId);
        exiledCards.addAll(graveyard);
        graveyard.clear();

        String logEntry = playerName + "'s graveyard is exiled (" + count + " card" + (count != 1 ? "s" : "") + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {}'s graveyard ({} cards) exiled", gameData.id, playerName, count);
    }

    /**
     * Resolves a {@link PutImprintedCreatureOntoBattlefieldEffect} by revealing the card imprinted
     * on the source and, if it is a creature card, putting it onto the battlefield under the
     * controller's control. Non-creature cards remain in exile.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (card must have an imprinted card)
     */
    @HandlesEffect(PutImprintedCreatureOntoBattlefieldEffect.class)
    void resolvePutImprintedCreatureOntoBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        Card imprintedCard = entry.getCard().getImprintedCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (imprintedCard == null) {
            String logMsg = entry.getCard().getName() + "'s imprint ability resolves but no card was imprinted.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String revealLog = playerName + " turns the exiled card face up: " + imprintedCard.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        boolean isCreature = imprintedCard.getType() == CardType.CREATURE
                || imprintedCard.getAdditionalTypes().contains(CardType.CREATURE);

        if (!isCreature) {
            String notCreatureLog = imprintedCard.getName() + " is not a creature card. It remains in exile.";
            gameBroadcastService.logAndBroadcast(gameData, notCreatureLog);
            return;
        }

        // Remove from exile zone
        List<Card> exiledCards = gameData.playerExiledCards.get(controllerId);
        if (exiledCards != null) {
            exiledCards.removeIf(c -> c.getId().equals(imprintedCard.getId()));
        }

        // Put onto the battlefield
        Permanent perm = new Permanent(imprintedCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String enterLog = imprintedCard.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);

        handleCreatureEtbAndLegendRule(gameData, controllerId, perm, imprintedCard);

        log.info("Game {} - {} puts imprinted creature {} onto battlefield",
                gameData.id, playerName, imprintedCard.getName());
    }

    /**
     * Resolves a {@link ReturnDyingCreatureToBattlefieldAndAttachSourceEffect} by returning a
     * creature that just died back to the battlefield and attaching the source equipment to it.
     * Used by equipment with triggered abilities like Nim Deathmantle. Fizzles if the dying
     * card is no longer in a graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (targetPermanentId is the source equipment)
     * @param effect   the return-and-attach effect (dyingCardId identifies the creature to return)
     */
    @HandlesEffect(ReturnDyingCreatureToBattlefieldAndAttachSourceEffect.class)
    void resolveReturnDyingCreatureAndAttachSource(GameData gameData, StackEntry entry,
                                                    ReturnDyingCreatureToBattlefieldAndAttachSourceEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Find the dying card in a graveyard
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, effect.dyingCardId());
        if (dyingCard == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (card is no longer in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Return+attach fizzles, card not in {}'s graveyard", gameData.id, playerName);
            return;
        }

        // Remove from graveyard
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCard.getId());

        // Put onto the battlefield
        Permanent creature = new Permanent(dyingCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, creature);

        String enterLog = dyingCard.getName() + " returns to the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);
        log.info("Game {} - {} returns {} to battlefield via {}", gameData.id, playerName, dyingCard.getName(), entry.getCard().getName());

        // Attach the source equipment to the returned creature
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (equipment != null) {
            equipment.setAttachedTo(creature.getId());
            String attachLog = entry.getCard().getName() + " is now attached to " + dyingCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, attachLog);
            log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), dyingCard.getName());
        }

        handleCreatureEtbAndLegendRule(gameData, controllerId, creature, dyingCard);
    }

    /**
     * Resolves a {@link CastTargetInstantOrSorceryFromGraveyardEffect} by
     * validating the targeted instant or sorcery card is still in a graveyard matching the scope,
     * then queuing a may-cast choice for the controller.
     */
    @HandlesEffect(CastTargetInstantOrSorceryFromGraveyardEffect.class)
    void resolveCastFromGraveyard(GameData gameData, StackEntry entry,
                                  CastTargetInstantOrSorceryFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Get the targeted card ID from targetCardIds (set at ETB trigger time)
        if (entry.getTargetCardIds().isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " — no target selected.");
            return;
        }

        UUID targetCardId = entry.getTargetCardIds().getFirst();
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target no longer in graveyard).");
            return;
        }

        // Verify target is still in a graveyard matching the scope
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target not in any graveyard).");
            return;
        }
        boolean validScope = switch (effect.scope()) {
            case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(controllerId);
            case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(controllerId);
            case ALL_GRAVEYARDS -> true;
        };
        if (!validScope) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target not in a valid graveyard).");
            return;
        }

        // Verify target is still an instant or sorcery
        if (targetCard.getType() != CardType.INSTANT && targetCard.getType() != CardType.SORCERY) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target is not an instant or sorcery).");
            return;
        }

        // Queue may-cast choice
        String prompt = effect.withoutPayingManaCost()
                ? entry.getCard().getName() + " — Cast " + targetCard.getName() + " without paying its mana cost?"
                : entry.getCard().getName() + " — Cast " + targetCard.getName() + "?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                targetCard,
                controllerId,
                List.of(effect),
                prompt
        ));
    }
}
