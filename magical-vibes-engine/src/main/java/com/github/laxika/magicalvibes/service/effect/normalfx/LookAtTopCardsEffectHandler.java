package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    @Lazy
    private GraveyardService graveyardService;
    private final LifeSupport lifeSupport;

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
        int chooseManaValueAtMost = e.chooseManaValueAtMost() == null
                ? Integer.MAX_VALUE
                : Math.max(0, amountEvaluationService.evaluate(gameData, e.chooseManaValueAtMost(), ctx));

        // Nothing to look at (e.g. Shrine of Piercing Vision with no charge counters).
        if (lookCount <= 0) {
            return;
        }

        if (e.chosenDestination() == LibrarySearchDestination.BATTLEFIELD
                || e.chosenDestination() == LibrarySearchDestination.BATTLEFIELD_TAPPED) {
            resolveMayPutOntoBattlefield(gameData, entry, e, lookCount, chooseCount, chooseManaValueAtMost);
        } else if (e.chosenDestination() == LibrarySearchDestination.TOP_OF_LIBRARY) {
            if (e.optional() && e.restDestination() == LookDestination.GRAVEYARD) {
                resolveMayPutOneOnTopRestToGraveyard(gameData, entry, lookCount);
            } else {
                resolvePutOneOnTop(gameData, entry, e, lookCount);
            }
        } else if (e.chosenDestination() == LibrarySearchDestination.GRAVEYARD
                && e.restDestination() == LookDestination.TOP_OF_LIBRARY) {
            resolveOneToGraveyardRestOnTop(gameData, entry, lookCount);
        } else if (e.chosenDestination() == LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM) {
            resolveOneToExilePlayableRestOnBottomRandom(gameData, entry, lookCount);
        } else if (e.optional()) {
            resolveMayRevealToHand(gameData, entry, e, lookCount, chooseCount, chooseManaValueAtMost);
        } else if (e.restDestination() == LookDestination.GRAVEYARD) {
            resolveRestToGraveyard(gameData, entry, e, lookCount, chooseCount);
        } else if (e.restDestination() == LookDestination.TOP_OF_LIBRARY) {
            resolveRestOnTop(gameData, entry, lookCount);
        } else if (e.restDestination() == LookDestination.EXILE) {
            resolveRestToExile(gameData, entry, lookCount, chooseCount);
        } else if (e.restDestination() == LookDestination.BOTTOM_OF_LIBRARY_RANDOM) {
            resolveRestToBottom(gameData, entry, e, lookCount, chooseCount, true);
        } else {
            resolveRestToBottom(gameData, entry, e, lookCount, chooseCount, false);
        }
    }

    // you may put matching card(s) onto the battlefield, rest to the effect's destination
    // (Mayael the Anima / Mitotic Manipulation: one card via LibrarySearch;
    //  Nissa, Genesis Mage −10: any number via LibraryRevealChoice + random bottom)

    private void resolveMayPutOntoBattlefield(GameData gameData, StackEntry entry,
            LookAtTopCardsEffect e, int lookCount, int chooseCount, int chooseManaValueAtMost) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, !e.reveal());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (e.reveal()) {
            GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals ");
            appendCardList(revealBuilder, topCards);
            revealBuilder.text(" from the top of their library with ").card(entry.getCard()).text(".");
            gameLogService.append(gameData, revealBuilder.build());
        }

        UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
        List<Card> matchingCards = topCards.stream()
                .filter(card -> card.getManaValue() <= chooseManaValueAtMost)
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, e.choosePredicate(), sourceCardId, gameData, controllerId))
                .toList();

        boolean randomBottom = e.restDestination() == LookDestination.BOTTOM_OF_LIBRARY_RANDOM;
        boolean remainingToExile = e.restDestination() == LookDestination.EXILE;
        boolean restToGraveyard = e.restDestination() == LookDestination.GRAVEYARD;
        boolean anyNumber = chooseCount > 1 || randomBottom;

        if (matchingCards.isEmpty()) {
            if (e.recordChosenCount()) {
                entry.setEventValue(0);
            }
            if (remainingToExile) {
                for (Card card : topCards) {
                    gameData.addToExile(controllerId, card);
                }
            } else if (restToGraveyard) {
                for (Card card : topCards) {
                    gameData.playerGraveyards.get(controllerId).add(card);
                }
                gameLogService.append(gameData, GameLog.text(
                        playerName + " puts the revealed cards into their graveyard."));
            } else if (randomBottom) {
                bottomInRandomOrder(gameData, controllerId, playerName, topCards);
            } else {
                libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            }
            return;
        }

        if (!anyNumber) {
            if (restToGraveyard) {
                String prompt = "Choose a card to put onto the battlefield. The rest go into your graveyard.";
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                        LibrarySearchParams.builder(controllerId, matchingCards)
                                .reveals(true)
                                .canFailToFind(e.optional())
                                .sourceCards(new ArrayList<>(topCards))
                        .restToGraveyard(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(e.chosenDestination())
                        .grantHaste(e.grantHaste())
                        .returnToHandAtEndStep(e.returnToHandAtEndStep())
                        .returnToHandAtControllerEndStepId(e.returnToHandAtEndStep()
                                ? entry.getControllerId() : null)
                        .build(),
                        prompt,
                        e.optional()));
                return;
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(controllerId, matchingCards)
                            .canFailToFind(true)
                            .sourceCards(topCards)
                            .reorderRemainingToBottom(!restToGraveyard && !remainingToExile)
                            .restToGraveyard(restToGraveyard)
                    .restToExile(remainingToExile)
                    .shuffleAfterSelection(false)
                    .prompt("You may put one of these cards onto the battlefield.")
                    .grantHaste(e.grantHaste())
                    .returnToHandAtEndStep(e.returnToHandAtEndStep())
                    .returnToHandAtControllerEndStepId(e.returnToHandAtEndStep()
                            ? entry.getControllerId() : null)
                    .destination(e.chosenDestination())
                            .build(),
                    "You may put one of these cards onto the battlefield.",
                    true));
            return;
        }

        int maxCount = Math.min(chooseCount, matchingCards.size());
        int minCount = e.exactChooseCount() ? maxCount : 0;
        String prompt = remainingToExile
                ? "Choose any number of eligible cards to put onto the battlefield. Exile the rest."
                : restToGraveyard
                ? "Choose any number of eligible cards to put onto the battlefield. The rest go into your graveyard."
                : e.cloakChosenPermanents()
                ? "Choose exactly " + minCount + " cards to cloak. The rest go to the bottom of your library in a random order."
                : randomBottom
                ? "Choose any number of eligible cards to put onto the battlefield. The rest go to the bottom of your library in a random order."
                : "Choose any number of eligible cards to put onto the battlefield. The rest go to the bottom of your library.";
        List<UUID> cardIds = matchingCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, restToGraveyard, false,
                !restToGraveyard && !randomBottom && !remainingToExile,
                randomBottom, remainingToExile, 0, null,
                maxCount, prompt, e.chosenDestination() == LibrarySearchDestination.BATTLEFIELD_TAPPED,
                minCount, e.gainLifeEqualToChosenCardManaValue(), e.effectIfNoCardChosen(),
                e.recordChosenCount(), e.cloakChosenPermanents(), false,
                e.battlefieldSelectionFollowUp(), false, false, false,
                e.selectedCardMayGoToHandIfBattlefieldDeclined()));
    }

    // ===== put one of the looked-at cards on top, rest on the bottom (Cream of the Crop) =====

    private void resolvePutOneOnTop(GameData gameData, StackEntry entry, LookAtTopCardsEffect e, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (topCards.size() == 1 && !e.optional()) {
            // Only one card looked at — it goes back on top, nothing to put on the bottom.
            gameData.playerDecks.get(controllerId).addFirst(topCards.getFirst());
            gameLogService.append(gameData, GameLog.text(playerName + " puts a card on top of their library."));
            return;
        }

        List<Card> matchingCards = e.choosePredicate() == null
                ? topCards
                : filterEligibleCards(topCards, e.choosePredicate(), entry.getCard().getId(), gameData, controllerId);
        if (matchingCards.isEmpty()) {
            if (e.restDestination() == LookDestination.BOTTOM_OF_LIBRARY_RANDOM) {
                bottomInRandomOrder(gameData, controllerId, playerName, topCards);
            } else {
                libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            }
            return;
        }

        List<Card> sourceCards = new ArrayList<>(topCards);
        boolean randomBottom = e.restDestination() == LookDestination.BOTTOM_OF_LIBRARY_RANDOM;
        String prompt = randomBottom
                ? "You may reveal a matching card from among them and put it on top of your library. "
                + "The rest go to the bottom of your library in a random order."
                : "Put one card on top of your library. The rest go to the bottom of your library.";
        LibrarySearchParams.Builder params = LibrarySearchParams.builder(controllerId, matchingCards)
                .canFailToFind(e.optional())
                .reveals(randomBottom)
                .sourceCards(sourceCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt(prompt)
                .destination(LibrarySearchDestination.TOP_OF_LIBRARY);
        if (randomBottom) {
            params.followUp(LibrarySearchFollowUp.forBoundedPick(
                    LibrarySearchFollowUp.SecondBoundedPick.terminal(true)));
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                params.build(), prompt, e.optional()));
    }

    private void resolveMayPutOneOnTopRestToGraveyard(GameData gameData, StackEntry entry, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;

        String prompt = "You may put one card on top of your library. The rest go into your graveyard.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), result.topCards())
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(result.topCards()))
                        .restToGraveyard(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                true));
    }

    private void resolveOneToGraveyardRestOnTop(GameData gameData, StackEntry entry, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;

        String prompt = "Put one card into your graveyard. Put the rest back on top of your library.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), result.topCards())
                        .sourceCards(new ArrayList<>(result.topCards()))
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.GRAVEYARD)
                        .build(),
                prompt,
                false));
    }

    private void resolveOneToExilePlayableRestOnBottomRandom(
            GameData gameData, StackEntry entry, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, false);
        if (result == null) return;

        String prompt = "Exile one card to play this turn. Put the rest on the bottom of your library in a random order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), result.topCards())
                        .sourceCards(new ArrayList<>(result.topCards()))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM)
                        .build(),
                prompt,
                false));
    }

    /**
     * One looked-at card into hand, the rest back on top of the library in an order the player
     * chooses (Diabolic Vision). The pick and the reorder both run through the
     * {@code LibrarySearch} flow, which puts the leftovers back on top when
     * {@code reorderRemainingToTop} is set.
     */
    private void resolveRestOnTop(GameData gameData, StackEntry entry, int lookCount) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        // A single looked-at card leaves nothing to put back on top.
        if (topCards.size() == 1) {
            gameData.addCardToHand(controllerId, topCards.getFirst());
            gameLogService.append(gameData, GameLog.text(
                    playerName + " looks at the top card of their library and puts it into their hand."));
            return;
        }

        String prompt = "Put one of these cards into your hand. The rest go back on top of your "
                + "library in any order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.HAND)
                        .build(),
                prompt,
                false));
    }

    /** No eligible card was found: every looked-at card goes to the bottom in a random order. */
    private void bottomInRandomOrder(GameData gameData, UUID controllerId, String playerName,
            List<Card> topCards) {
        java.util.Collections.shuffle(topCards);
        gameData.playerDecks.get(controllerId).addAll(topCards);
        gameLogService.append(gameData, GameLog.text(playerName
                + " finds no eligible cards. All cards are put on the bottom of their library in a random order."));
    }

    private void putOnBottomInRandomOrder(GameData gameData, UUID controllerId, String playerName,
            List<Card> topCards) {
        java.util.Collections.shuffle(topCards);
        gameData.playerDecks.get(controllerId).addAll(topCards);
        gameLogService.append(gameData, GameLog.text(playerName
                + " puts all looked-at cards on the bottom of their library in a random order."));
    }

    private void resolveMayRevealToHand(GameData gameData, StackEntry entry,
            LookAtTopCardsEffect e, int lookCount, int chooseCount, int chooseManaValueAtMost) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, false);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        boolean toGraveyard = e.restDestination() == LookDestination.GRAVEYARD;
        boolean randomBottom = e.restDestination() == LookDestination.BOTTOM_OF_LIBRARY_RANDOM;

        if (e.reveal()) {
            GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals ");
            appendCardList(revealBuilder, topCards);
            revealBuilder.text(" from the top of their library with ").card(entry.getCard()).text(".");
            gameLogService.append(gameData, revealBuilder.build());
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " looks at the top "
                    + LibraryRevealSupport.pluralCards(topCards.size()) + " of their library."));
        }

        List<Card> matchingCards = filterEligibleCards(topCards, e.choosePredicate(),
                entry.getCard().getId(), gameData, controllerId).stream()
                .filter(card -> card.getManaValue() <= chooseManaValueAtMost)
                .toList();
        if (matchingCards.isEmpty()) {
            if (toGraveyard) {
                for (Card card : topCards) {
                    graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
                }
                GameLog.Builder restBuilder = GameLog.builder().text(playerName + " puts ");
                appendCardList(restBuilder, topCards);
                restBuilder.text(" into their graveyard.");
                gameLogService.append(gameData, restBuilder.build());
            } else if (randomBottom) {
                bottomInRandomOrder(gameData, controllerId, playerName, topCards);
                insertEffectAfterCurrent(entry, e, e.effectIfNoCardChosen());
            } else {
                libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            }
            return;
        }

        if (chooseCount <= 0) {
            if (randomBottom) {
                putOnBottomInRandomOrder(gameData, controllerId, playerName, topCards);
            } else {
                libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            }
            insertEffectAfterCurrent(entry, e, e.effectIfNoCardChosen());
            return;
        }

        String description = CardPredicateUtils.describeFilter(e.choosePredicate());
        if (chooseCount > 1 || randomBottom) {
            List<UUID> cardIds = matchingCards.stream().map(Card::getId).toList();
            int max = Math.min(chooseCount, matchingCards.size());
            if (e.payLifePerSelectedCard() > 0) {
                int affordable = gameQueryService.canPlayerLifeChange(gameData, controllerId)
                        ? gameData.getLife(controllerId) / e.payLifePerSelectedCard()
                        : 0;
                max = Math.min(max, affordable);
                if (max == 0) {
                    for (Card card : topCards) {
                        graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
                    }
                    gameLogService.append(gameData, GameLog.text(playerName
                            + " cannot pay for any of the revealed cards, so they are put into the graveyard."));
                    return;
                }
            }
            String revealPrompt;
            if (max == 1) {
                revealPrompt = e.payLifePerSelectedCard() > 0
                        ? "You may put a card into your hand by paying "
                                + e.payLifePerSelectedCard() + " life."
                        : e.reveal()
                        ? "You may put a " + description + " from among them into your hand."
                        : "You may reveal a " + description + " from among them and put it into your hand.";
            } else {
                revealPrompt = e.payLifePerSelectedCard() > 0
                        ? "You may put up to " + max + " cards into your hand by paying "
                                + e.payLifePerSelectedCard() + " life for each."
                        : e.reveal()
                        ? (chooseCount >= Integer.MAX_VALUE
                                ? "You may put any number of " + description + "s into your hand."
                                : "You may put up to " + max + " " + description + "s into your hand.")
                        : (chooseCount >= Integer.MAX_VALUE
                                ? "You may reveal any number of " + description + "s and put them into your hand."
                                : "You may reveal up to " + max + " " + description + "s and put them into your hand.");
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                    controllerId, topCards, cardIds, toGraveyard, true,
                    !toGraveyard && !randomBottom, randomBottom, false,
                    e.payLifePerSelectedCard() > 0
                            ? e.payLifePerSelectedCard() : e.loseLifePerSelectedCard(),
                    null, max, revealPrompt, false, 0, false, e.effectIfNoCardChosen(), false,
                    false, e.payLifePerSelectedCard() > 0, null, false, false));
            return;
        }

        String prompt = e.reveal()
                ? "You may put a " + description + " from among them into your hand."
                : "You may reveal a " + description + " from among them and put it into your hand.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, matchingCards)
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(topCards)
                        .reorderRemainingToBottom(!toGraveyard)
                        .restToGraveyard(toGraveyard)
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
                gameLogService.append(gameData, builder.build());
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

    // ===== rest on the bottom of the library (Stress Dream / Shrine / Jar of Eyeballs;
    //       Memory Deluge when randomRemaining) =====

    private void resolveRestToBottom(GameData gameData, StackEntry entry, LookAtTopCardsEffect e,
            int lookCount, int chooseCount, boolean randomRemaining) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (e.reveal()) {
            GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals ");
            appendCardList(revealBuilder, topCards);
            revealBuilder.text(" from the top of their library with ").card(entry.getCard()).text(".");
            gameLogService.append(gameData, revealBuilder.build());
        }

        if (e.choosePredicate() != null) {
            resolveRestToBottomFiltered(gameData, entry, controllerId, topCards, playerName, e,
                    chooseCount, randomRemaining);
            return;
        }

        // Not enough cards to choose from: they simply go to hand, nothing on bottom.
        if (topCards.size() <= chooseCount) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            if (!topCards.isEmpty()) {
                GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
                appendCardList(builder, topCards);
                builder.text(" into their hand.");
                gameLogService.append(gameData, builder.build());
            }
            return;
        }

        String handWord = chooseCount == 1 ? "one" : String.valueOf(chooseCount);
        String restPhrase = randomRemaining
                ? "the rest on the bottom of your library in a random order."
                : "the rest on the bottom of your library.";
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds,
                false, true, !randomRemaining, randomRemaining, false, 0, null, chooseCount,
                "Look at the top " + topCards.size() + " cards of your library. Put " + handWord
                        + " into your hand and " + restPhrase));
    }

    /**
     * Bottom-of-library variant restricted by {@code choosePredicate}: only matching cards are
     * eligible for hand, everything else goes to the bottom. With {@code chooseCount} at least the
     * number of eligible cards this is choice-free — every matching card auto-moves to hand
     * (Lair Delve).
     */
    private void resolveRestToBottomFiltered(GameData gameData, StackEntry entry, UUID controllerId,
            List<Card> topCards, String playerName, LookAtTopCardsEffect e, int chooseCount,
            boolean randomRemaining) {
        List<Card> eligibleCards = filterEligibleCards(topCards, e.choosePredicate(),
                entry.getCard().getId(), gameData, controllerId);

        if (eligibleCards.size() > chooseCount) {
            List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
            String handWord = chooseCount == 1 ? "one" : String.valueOf(chooseCount);
            String restPhrase = randomRemaining
                    ? "the rest on the bottom of your library in a random order."
                    : "the rest on the bottom of your library.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                    controllerId, topCards, cardIds,
                    false, true, !randomRemaining, randomRemaining, false, 0, null, chooseCount,
                    "Put " + handWord + " " + CardPredicateUtils.describeFilter(e.choosePredicate())
                            + " into your hand and " + restPhrase));
            return;
        }

        for (Card card : eligibleCards) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!eligibleCards.isEmpty()) {
            GameLog.Builder handBuilder = GameLog.builder().text(playerName + " puts ");
            appendCardList(handBuilder, eligibleCards);
            handBuilder.text(" into their hand.");
            gameLogService.append(gameData, handBuilder.build());
        }

        List<Card> remainingCards = new ArrayList<>(topCards);
        remainingCards.removeAll(eligibleCards);
        if (!remainingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, remainingCards);
        }
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
            gameLogService.append(gameData, revealBuilder.build());
        }

        if (handChoicePredicate == null) {
            resolveWithoutPredicate(gameData, entry, controllerId, topCards, playerName, count, toHandCount,
                    e.gainLifeEqualToChosenCardManaValue(), e.exactChooseCount());
            return;
        }

        List<Card> eligibleCards = filterEligibleCards(topCards, handChoicePredicate,
                entry.getCard().getId(), gameData, controllerId);

        if (eligibleCards.isEmpty()) {
            for (Card card : topCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
            }
            GameLog.Builder restBuilder = GameLog.builder().text(playerName + " puts ");
            appendCardList(restBuilder, topCards);
            restBuilder.text(" into their graveyard.");
            gameLogService.append(gameData, restBuilder.build());
            log.info("Game {} - {} resolving {} — 0 eligible, {} to graveyard",
                    gameData.id, playerName, cardName, topCards.size());
            return;
        }

        if (eligibleCards.size() <= toHandCount) {
            for (Card card : eligibleCards) {
                gameData.addCardToHand(controllerId, card);
            }
            if (e.gainLifeEqualToChosenCardManaValue() && !eligibleCards.isEmpty()) {
                gainLifeForChosenCard(gameData, entry, eligibleCards.getFirst());
            }
            List<Card> remainingCards = new ArrayList<>(topCards);
            remainingCards.removeAll(eligibleCards);
            for (Card card : remainingCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
            }

            GameLog.Builder handBuilder = GameLog.builder().text(playerName + " puts ");
            appendCardList(handBuilder, eligibleCards);
            handBuilder.text(" into their hand.");
            gameLogService.append(gameData, handBuilder.build());
            if (!remainingCards.isEmpty()) {
                GameLog.Builder restBuilder = GameLog.builder().text(playerName + " puts ");
                appendCardList(restBuilder, remainingCards);
                restBuilder.text(" into their graveyard.");
                gameLogService.append(gameData, restBuilder.build());
            }
            return;
        }

        String handWord = toHandCount == 1 ? "one" : String.valueOf(toHandCount);
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        String actionVerb = e.reveal() ? "Reveal" : "Look at";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, true, true, false, false, false, 0, null, toHandCount,
                actionVerb + " the top " + count + " cards of your library. Put " + handWord
                        + " into your hand. The rest are put into your graveyard.",
                e.exactChooseCount()
                        ? Math.min(toHandCount, eligibleCards.size())
                        : e.gainLifeEqualToChosenCardManaValue() ? 1 : 0,
                e.gainLifeEqualToChosenCardManaValue()));

        if (!e.reveal()) {
            gameLogService.append(gameData, GameLog.text(playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library."));
        }
        log.info("Game {} - {} resolving {} with {} cards, {} eligible",
                gameData.id, playerName, cardName, count, eligibleCards.size());
    }

    private void resolveWithoutPredicate(GameData gameData, StackEntry entry, UUID controllerId,
            List<Card> topCards, String playerName, int count, int toHandCount,
            boolean gainLifeEqualToChosenCardManaValue, boolean exactChooseCount) {
        if (count <= toHandCount) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            if (gainLifeEqualToChosenCardManaValue && !topCards.isEmpty()) {
                gainLifeForChosenCard(gameData, entry, topCards.getFirst());
            }
            String logMsg = count == 1
                    ? playerName + " looks at the top card of their library and puts it into their hand."
                    : playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count)
                            + " of their library and puts them into their hand.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        String handWord = toHandCount == 1 ? "one" : String.valueOf(toHandCount);
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds, true, true, false, false, false, 0, null, toHandCount,
                "Look at the top " + count + " cards of your library. Put " + handWord
                        + " into your hand. The rest are put into your graveyard.",
                exactChooseCount
                        ? Math.min(toHandCount, topCards.size())
                        : gainLifeEqualToChosenCardManaValue ? 1 : 0,
                gainLifeEqualToChosenCardManaValue));

        gameLogService.append(gameData, GameLog.text(playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library."));
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

    private static void insertEffectAfterCurrent(StackEntry entry, CardEffect currentEffect,
            CardEffect followUp) {
        if (followUp == null) {
            return;
        }
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == currentEffect) {
                entry.insertEffectsToResolve(i + 1, List.of(followUp));
                return;
            }
        }
        throw new IllegalStateException("Look-at-top effect is not present on its stack entry");
    }

    private List<Card> filterEligibleCards(List<Card> topCards, CardPredicate predicate,
            UUID sourceCardId, GameData gameData, UUID controllerId) {
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : topCards) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, predicate, sourceCardId, gameData, controllerId)) {
                eligibleCards.add(card);
            }
        }
        return eligibleCards;
    }

    private void gainLifeForChosenCard(GameData gameData, StackEntry entry, Card chosenCard) {
        int manaValue = chosenCard.getManaValue();
        if (manaValue <= 0) {
            return;
        }
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), manaValue,
                entry.getCard().getName(), entry.getCard(), entry.getEntryType());
    }
}
