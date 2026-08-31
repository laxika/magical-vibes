package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unified handler for {@link SearchLibraryEffect}: searches the controller's library for up to
 * {@code count} cards matching the effect's filter (plus an optional X-relative mana-value bound)
 * and routes them to the effect's {@link LibrarySearchDestination} through the shared
 * {@link LibrarySearchSupport}/{@code LibraryChoiceHandlerService} interaction pipeline.
 *
 * <p>Collapses the former {@code SearchLibraryFor*} family (to-hand tutors, by-name searches,
 * to-top, creature-to-battlefield with MV/colour/subtype constraints, card-types-to-battlefield).
 * Reveal / fail-to-find behaviour is derived uniformly: a restricted search (non-null filter or a
 * mana-value bound) can fail to find, and reveals its pick for {@code HAND}/{@code TOP_OF_LIBRARY}
 * destinations; an unrestricted search does neither.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryEffect) effect, LibrarySearchFollowUp.NONE, null);
    }

    void resolveWithFollowUp(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                             LibrarySearchFollowUp followUp) {
        doResolve(gameData, entry, effect, followUp, null);
    }

    void resolveWithTotalManaValueCap(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                                      int maxTotalManaValue) {
        doResolve(gameData, entry, effect, LibrarySearchFollowUp.NONE, maxTotalManaValue);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryEffect effect,
                           LibrarySearchFollowUp followUp, Integer totalManaValueBound) {
        UUID controllerId = effect.searchPlayer() == LibrarySearchPlayer.ACTIVE_PLAYER
                ? entry.getActivePlayerId() : entry.getControllerId();
        if (controllerId == null) return;
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, effect.shuffleAfterSelection())) return;

        AmountContext amountContext = AmountContext.forStackEntry(entry, resolveSource(gameData, entry));

        int count = entry.isCastWithFlashback()
                ? effect.castFromGraveyardCount()
                : Math.max(0, amountEvaluationService.evaluate(gameData, effect.count(), amountContext));

        CardPredicate filter = effect.filter();
        ManaValueBound bound = effect.manaValueBound();
        boolean restricted = filter != null || bound != null || totalManaValueBound != null;
        Integer boundValue = bound == null ? null
                : amountEvaluationService.evaluate(gameData, bound.amount(), amountContext) + bound.offset();

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            if (effect.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty."
                    + (effect.shuffleAfterSelection() ? " Library is shuffled." : "")));
            return;
        }

        // "Up to X" with X=0 still searches and shuffles (Uncage the Menagerie ruling).
        if (count <= 0) {
            if (effect.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library."
                    + (effect.shuffleAfterSelection() ? " Library is shuffled." : "")));
            return;
        }

        Predicate<Card> deckFilter = card ->
                (filter == null || predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId))
                        && matchesBound(card, boundValue, bound)
                        && (totalManaValueBound == null || card.getManaValue() <= totalManaValueBound)
                        && (!putsOntoBattlefield(effect.destination())
                        || !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY));
        List<Card> matchingCards = deck.stream().filter(deckFilter).toList();

        String baseDesc = describe(filter, boundValue, bound);
        if (totalManaValueBound != null) {
            baseDesc += " with total mana value " + totalManaValueBound + " or less";
        }

        if (totalManaValueBound != null) {
            count = Math.min(count, matchingCards.size());
        }

        if (matchingCards.isEmpty()) {
            if (!librarySearchSupport.librarySearchCastableCards(gameData, controllerId).isEmpty()) {
                LibrarySearchDestination destination = effect.destination();
                String prompt = buildPrompt(baseDesc, destination, restricted, count,
                        effect.requireDifferentNames());
                librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                        LibrarySearchParams.builder(controllerId, new ArrayList<>())
                                .remainingCount(count)
                                .canFailToFind(true)
                                .destination(destination)
                                .filterPredicate(restricted ? filter : null)
                                .requireDifferentNames(effect.requireDifferentNames())
                                .manaValueBound(boundValue, bound != null && bound.exact())
                                .totalManaValueBound(totalManaValueBound)
                                .grantHaste(effect.grantHaste())
                                .exileAtEndStep(effect.exileAtEndStep())
                                .returnToHandAtEndStep(effect.returnToHandAtEndStep())
                                .animateFound(effect.animateFound())
                                .placeBattlefieldCardsSimultaneously(effect.animateFound() != null)
                                .battlefieldCounter(effect.battlefieldCounter())
                                .followUp(followUp)
                                .shuffleAfterSelection(effect.shuffleAfterSelection())
                                .battlefieldIfChosenBeholdType(effect.battlefieldIfChosenBeholdType()
                                        ? entry.getBeholdChosenSubtype() : null)
                                .build(),
                        prompt, true);
                return;
            }
            if (effect.shuffleAfterSelection()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            // Pluralize the target description ("artifact card" -> "artifact cards", "card named X"
            // -> "cards named X") by promoting the first whole-word "card"; a mana-value-bound
            // description stays singular ("creature card with mana value N").
            String noMatchDesc = bound != null ? baseDesc : baseDesc.replaceFirst("\\bcard\\b", "cards");
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no " + noMatchDesc + "."
                    + (effect.shuffleAfterSelection() ? " Library is shuffled." : "")));
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDesc);
            return;
        }

        LibrarySearchDestination destination = effect.destination();
        String prompt = buildPrompt(baseDesc, destination, restricted, count, effect.requireDifferentNames());

        LibrarySearchParams.Builder params = LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(count)
                        .reveals(reveals(restricted, destination))
                        .canFailToFind(restricted)
                        .destination(destination)
                        .filterPredicate(restricted ? filter : null)
                        .requireDifferentNames(effect.requireDifferentNames())
                        .manaValueBound(boundValue, bound != null && bound.exact())
                        .totalManaValueBound(totalManaValueBound)
                        .grantHaste(effect.grantHaste())
                        .exileAtEndStep(effect.exileAtEndStep())
                        .returnToHandAtEndStep(effect.returnToHandAtEndStep())
                        .animateFound(effect.animateFound())
                        .placeBattlefieldCardsSimultaneously(effect.animateFound() != null)
                        .battlefieldCounter(effect.battlefieldCounter())
                        .followUp(followUp)
                        .enterWithCounters(effect.enterWithCounters())
                        .shuffleAfterSelection(effect.shuffleAfterSelection())
                        .battlefieldIfChosenBeholdType(effect.battlefieldIfChosenBeholdType()
                                ? entry.getBeholdChosenSubtype() : null);
        if (destination == LibrarySearchDestination.BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER) {
            params.battlefieldControllerId(entry.getTargetId());
        }
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, params.build(),
                prompt, restricted);

        log.info("Game {} - {} searches library for {} card(s) to {} ({} matches)",
                gameData.id, playerName, count, destination, matchingCards.size());
    }

    private Permanent resolveSource(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    private boolean matchesBound(Card card, Integer boundValue, ManaValueBound bound) {
        if (boundValue == null) return true;
        return bound.exact()
                ? card.getManaValue() == boundValue
                : card.getManaValue() <= boundValue;
    }

    private boolean putsOntoBattlefield(LibrarySearchDestination destination) {
        return switch (destination) {
            case BATTLEFIELD, BATTLEFIELD_TAPPED, BATTLEFIELD_ATTACHED_TO_PLAYER,
                    BATTLEFIELD_ATTACHED_TO_CREATURE, BATTLEFIELD_ATTACHED_TO_PERMANENT,
                    BATTLEFIELD_UNDER_SEARCHER -> true;
            default -> false;
        };
    }

    /** Human description of the search target, e.g. "creature card with mana value 3 or less". */
    private String describe(CardPredicate filter, Integer boundValue, ManaValueBound bound) {
        String desc = CardPredicateUtils.describeFilter(filter);
        if (boundValue != null) {
            desc += bound.exact()
                    ? " with mana value " + boundValue
                    : " with mana value " + boundValue + " or less";
        }
        return desc;
    }

    private boolean reveals(boolean restricted, LibrarySearchDestination destination) {
        return restricted && (destination == LibrarySearchDestination.HAND
                || destination == LibrarySearchDestination.TOP_OF_LIBRARY);
    }

    private String buildPrompt(String desc, LibrarySearchDestination destination,
                               boolean restricted, int count, boolean requireDifferentNames) {
        String remaining = count > 1 ? " (" + count + " remaining)" : "";
        String distinct = requireDifferentNames ? " with a different name" : "";
        return switch (destination) {
            case HAND -> "Search your library for a " + desc + distinct
                    + (restricted ? " to reveal and put into your hand" : " to put into your hand")
                    + remaining + ".";
            case TOP_OF_LIBRARY -> "Search your library for a " + desc
                    + (restricted
                            ? ", reveal it, then shuffle and put that card on top."
                            : ", then shuffle and put that card on top.");
            case EXILE -> "Search your library for a " + desc + " to exile" + remaining + ".";
            case EXILE_FOR_MAY_CAST -> "Search your library for a " + desc + " to exile" + remaining + ".";
            case EXILE_PLAYABLE_ANY_NUMBER -> "Search your library for matching cards to exile (any number).";
            case GRAVEYARD -> count > 1
                    ? "Search your library for a " + desc + " to put into your graveyard" + remaining + "."
                    : "Search your library for a " + desc + " and put it into your graveyard.";
            case BATTLEFIELD_TAPPED -> count > 1
                    ? "Search your library for a " + desc + " to put onto the battlefield tapped" + remaining + "."
                    : "Search your library for a " + desc + " and put it onto the battlefield tapped.";
            case BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER -> "Search your library for a " + desc
                    + " and put it onto the battlefield tapped under target player's control.";
            default -> count > 1
                    ? "Search your library for a " + desc + " to put onto the battlefield" + remaining + "."
                    : "Search your library for a " + desc + " and put it onto the battlefield.";
        };
    }
}
