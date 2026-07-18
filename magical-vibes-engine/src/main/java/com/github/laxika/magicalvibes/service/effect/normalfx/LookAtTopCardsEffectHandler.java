package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Look at (or reveal) the top N cards of your library, choose up to M of them (optionally
 * filtered) for the chosen destination, and put the rest to the rest destination. Handles
 * {@link LookAtTopCardsEffect}, the collapsed "look at top N, choose some" family. Each folded
 * subfamily keeps its original flow verbatim: the mandatory to-hand flows feed
 * {@link PendingInteraction.LibraryRevealChoice}, the optional ("may") single pick, the
 * battlefield destination and the put-one-on-top destination feed
 * {@link PendingInteraction.LibrarySearch}, and the optional multi pick feeds
 * {@link PendingInteraction.LibraryRevealChoice}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsEffect e = (LookAtTopCardsEffect) effect;

        // Source-relative amounts (CountersOnSource for Shrine of Piercing Vision) use the live
        // source permanent when it is still on the battlefield, else the last-known snapshot — the
        // source is sacrificed as a cost (Mill/Grindclock precedent).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int lookCount = Math.max(0, amountEvaluationService.evaluate(gameData, e.lookCount(), ctx));
        int chooseCount = Math.max(0, amountEvaluationService.evaluate(gameData, e.chooseCount(), ctx));

        // Nothing to look at (e.g. Shrine of Piercing Vision with no charge counters).
        if (lookCount <= 0) {
            return;
        }

        if (e.chosenDestination() == LibrarySearchDestination.BATTLEFIELD) {
            resolveMayPutOntoBattlefield(gameData, entry, e, lookCount);
        } else if (e.chosenDestination() == LibrarySearchDestination.TOP_OF_LIBRARY) {
            resolvePutOneOnTop(gameData, entry, lookCount);
        } else if (e.optional()) {
            resolveMayRevealToHand(gameData, entry, e, lookCount, chooseCount);
        } else if (e.restDestination() == LookDestination.GRAVEYARD) {
            resolveRestToGraveyard(gameData, entry, e, lookCount, chooseCount);
        } else if (e.restDestination() == LookDestination.EXILE) {
            resolveRestToExile(gameData, entry, lookCount, chooseCount);
        } else {
            resolveRestToBottom(gameData, entry, lookCount, chooseCount);
        }
    }

    // ===== you may put one matching card onto the battlefield, rest on the bottom =====
    // (Mayael the Anima; Mitotic Manipulation via CardSharesNameWithAPermanentPredicate)

    private void resolveMayPutOntoBattlefield(GameData gameData, StackEntry entry,
            LookAtTopCardsEffect e, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
        List<Card> matchingCards = topCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, e.choosePredicate(), sourceCardId, gameData, controllerId))
                .toList();

        if (matchingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, matchingCards)
                        .canFailToFind(true)
                        .sourceCards(topCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt("You may put one of these cards onto the battlefield.")
                        .destination(LibrarySearchDestination.BATTLEFIELD)
                        .build(),
                "You may put one of these cards onto the battlefield.",
                true));
    }

    // ===== put one of the looked-at cards on top, rest on the bottom (Cream of the Crop) =====

    private void resolvePutOneOnTop(GameData gameData, StackEntry entry, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (topCards.size() == 1) {
            // Only one card looked at — it goes back on top, nothing to put on the bottom.
            gameData.playerDecks.get(controllerId).addFirst(topCards.getFirst());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts a card on top of their library."));
            return;
        }

        List<Card> sourceCards = new ArrayList<>(topCards);
        String prompt = "Put one card on top of your library. The rest go to the bottom of your library.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .sourceCards(sourceCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                false));
    }

    // ===== you may reveal matching cards and put them into your hand, rest on the bottom =====
    // (Commune with Nature single pick; Lead the Stampede / Follow the Lumarets multi pick)

    private void resolveMayRevealToHand(GameData gameData, StackEntry entry,
            LookAtTopCardsEffect e, int lookCount, int chooseCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        List<Card> matchingCards = filterEligibleCards(topCards, e.choosePredicate(), gameData, controllerId);
        if (matchingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        String description = CardPredicateUtils.describeFilter(e.choosePredicate());
        if (chooseCount > 1) {
            List<UUID> cardIds = matchingCards.stream().map(Card::getId).toList();
            int max = Math.min(chooseCount, matchingCards.size());
            String revealPrompt = chooseCount >= Integer.MAX_VALUE
                    ? "You may reveal any number of " + description + "s and put them into your hand."
                    : "You may reveal up to " + max + " " + description + "s and put them into your hand.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                    controllerId, topCards, cardIds, false, true, true, false, false, 0, null,
                    max, revealPrompt));
            return;
        }

        String prompt = "You may reveal a " + description + " from among them and put it into your hand.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, matchingCards)
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(topCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .build(),
                prompt,
                true));
    }

    // ===== exile the rest (Browse) =====

    private void resolveRestToExile(GameData gameData, StackEntry entry, int lookCount, int chooseCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        // Not enough cards to choose from: they simply go to hand, nothing exiled.
        if (topCards.size() <= chooseCount) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            if (!topCards.isEmpty()) {
                GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
                appendCardList(builder, topCards);
                builder.text(" into their hand.");
                gameBroadcastService.logAndBroadcast(gameData, builder.build());
            }
            return;
        }

        String handWord = chooseCount == 1 ? "one" : String.valueOf(chooseCount);
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds,
                false, true, false, false, true, 0, null, chooseCount,
                "Look at the top " + topCards.size() + " cards of your library. Put " + handWord
                        + " into your hand and exile the rest."));
    }

    // ===== rest on the bottom of the library (Stress Dream / Shrine / Jar of Eyeballs) =====

    private void resolveRestToBottom(GameData gameData, StackEntry entry, int lookCount, int chooseCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        // Not enough cards to choose from: they simply go to hand, nothing on bottom.
        if (topCards.size() <= chooseCount) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            if (!topCards.isEmpty()) {
                GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
                appendCardList(builder, topCards);
                builder.text(" into their hand.");
                gameBroadcastService.logAndBroadcast(gameData, builder.build());
            }
            return;
        }

        String handWord = chooseCount == 1 ? "one" : String.valueOf(chooseCount);
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds,
                false, true, true, false, false, 0, null, chooseCount,
                "Look at the top " + topCards.size() + " cards of your library. Put " + handWord
                        + " into your hand and the rest on the bottom of your library."));
    }

    // ===== rest into the graveyard (Forbidden Alchemy / Dark Bargain / Tower Geist / Tracker's) =====

    private void resolveRestToGraveyard(GameData gameData, StackEntry entry, LookAtTopCardsEffect e,
            int lookCount, int chooseCount) {
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();
        int count = topCards.size();
        int toHandCount = chooseCount;
        CardPredicate handChoicePredicate = e.choosePredicate();

        if (e.reveal()) {
            GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals ");
            appendCardList(revealBuilder, topCards);
            revealBuilder.text(" from the top of their library with ").card(entry.getCard()).text(".");
            gameBroadcastService.logAndBroadcast(gameData, revealBuilder.build());
        }

        if (handChoicePredicate == null) {
            resolveWithoutPredicate(gameData, entry, controllerId, topCards, playerName, count, toHandCount);
            return;
        }

        List<Card> eligibleCards = filterEligibleCards(topCards, handChoicePredicate, gameData, controllerId);

        if (eligibleCards.isEmpty()) {
            for (Card card : topCards) {
                gameData.playerGraveyards.get(controllerId).add(card);
            }
            GameLog.Builder restBuilder = GameLog.builder().text(playerName + " puts ");
            appendCardList(restBuilder, topCards);
            restBuilder.text(" into their graveyard.");
            gameBroadcastService.logAndBroadcast(gameData, restBuilder.build());
            log.info("Game {} - {} resolving {} — 0 eligible, {} to graveyard",
                    gameData.id, playerName, cardName, topCards.size());
            return;
        }

        if (eligibleCards.size() <= toHandCount) {
            for (Card card : eligibleCards) {
                gameData.addCardToHand(controllerId, card);
            }
            List<Card> remainingCards = new ArrayList<>(topCards);
            remainingCards.removeAll(eligibleCards);
            for (Card card : remainingCards) {
                gameData.playerGraveyards.get(controllerId).add(card);
            }

            GameLog.Builder handBuilder = GameLog.builder().text(playerName + " puts ");
            appendCardList(handBuilder, eligibleCards);
            handBuilder.text(" into their hand.");
            gameBroadcastService.logAndBroadcast(gameData, handBuilder.build());
            if (!remainingCards.isEmpty()) {
                GameLog.Builder restBuilder = GameLog.builder().text(playerName + " puts ");
                appendCardList(restBuilder, remainingCards);
                restBuilder.text(" into their graveyard.");
                gameBroadcastService.logAndBroadcast(gameData, restBuilder.build());
            }
            return;
        }

        String handWord = toHandCount == 1 ? "one" : String.valueOf(toHandCount);
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        String actionVerb = e.reveal() ? "Reveal" : "Look at";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, true, true, false, false, false, 0, null, toHandCount,
                actionVerb + " the top " + count + " cards of your library. Put " + handWord
                        + " into your hand. The rest are put into your graveyard."));

        if (!e.reveal()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library."));
        }
        log.info("Game {} - {} resolving {} with {} cards, {} eligible",
                gameData.id, playerName, cardName, count, eligibleCards.size());
    }

    private void resolveWithoutPredicate(GameData gameData, StackEntry entry, UUID controllerId,
            List<Card> topCards, String playerName, int count, int toHandCount) {
        if (count <= toHandCount) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            String logMsg = count == 1
                    ? playerName + " looks at the top card of their library and puts it into their hand."
                    : playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count)
                            + " of their library and puts them into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        String handWord = toHandCount == 1 ? "one" : String.valueOf(toHandCount);
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, true, true, false, false, false, 0, null, toHandCount,
                "Look at the top " + count + " cards of your library. Put " + handWord
                        + " into your hand. The rest are put into your graveyard."));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library."));
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), count);
    }

    /** Appends {@code cards} to {@code builder} as comma-separated card segments. */
    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }

    private List<Card> filterEligibleCards(List<Card> topCards, CardPredicate predicate,
            GameData gameData, UUID controllerId) {
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : topCards) {
            if (predicateEvaluationService.matchesCardPredicate(card, predicate, null, gameData, controllerId)) {
                eligibleCards.add(card);
            }
        }
        return eligibleCards;
    }
}
