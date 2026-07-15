package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        AmountContext amountContext = AmountContext.forStackEntry(entry, resolveSource(gameData, entry));

        int count = entry.isCastWithFlashback()
                ? effect.castFromGraveyardCount()
                : Math.max(0, amountEvaluationService.evaluate(gameData, effect.count(), amountContext));

        CardPredicate filter = effect.filter();
        ManaValueBound bound = effect.manaValueBound();
        boolean restricted = filter != null || bound != null;
        Integer boundValue = bound == null ? null
                : amountEvaluationService.evaluate(gameData, bound.amount(), amountContext) + bound.offset();

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        Predicate<Card> deckFilter = card ->
                (filter == null || predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId))
                        && matchesBound(card, boundValue, bound);
        List<Card> matchingCards = deck.stream().filter(deckFilter).toList();

        String baseDesc = describe(filter, boundValue, bound);

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            // Pluralize the target description ("artifact card" -> "artifact cards", "card named X"
            // -> "cards named X") by promoting the first whole-word "card"; a mana-value-bound
            // description stays singular ("creature card with mana value N").
            String noMatchDesc = bound != null ? baseDesc : baseDesc.replaceFirst("\\bcard\\b", "cards");
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but finds no " + noMatchDesc + ". Library is shuffled."));
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDesc);
            return;
        }

        LibrarySearchDestination destination = effect.destination();
        String prompt = buildPrompt(baseDesc, destination, restricted, count);

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(count)
                        .reveals(reveals(restricted, destination))
                        .canFailToFind(restricted)
                        .destination(destination)
                        .filterPredicate(restricted ? filter : null)
                        .build(),
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
                               boolean restricted, int count) {
        String remaining = count > 1 ? " (" + count + " remaining)" : "";
        return switch (destination) {
            case HAND -> "Search your library for a " + desc
                    + (restricted ? " to reveal and put into your hand" : " to put into your hand")
                    + remaining + ".";
            case TOP_OF_LIBRARY -> "Search your library for a " + desc
                    + (restricted
                            ? ", reveal it, then shuffle and put that card on top."
                            : ", then shuffle and put that card on top.");
            case BATTLEFIELD_TAPPED -> count > 1
                    ? "Search your library for a " + desc + " to put onto the battlefield tapped" + remaining + "."
                    : "Search your library for a " + desc + " and put it onto the battlefield tapped.";
            default -> count > 1
                    ? "Search your library for a " + desc + " to put onto the battlefield" + remaining + "."
                    : "Search your library for a " + desc + " and put it onto the battlefield.";
        };
    }
}
