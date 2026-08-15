package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SearchTargetLibraryEffect}: the controller searches the targeted player's library
 * for up to N matching cards and the per-card loop is driven by
 * {@link com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService} through the
 * effect's {@link LibrarySearchDestination}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchTargetLibraryEffect e = (SearchTargetLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Leonin Arbiter: the search itself does not happen, but "then that player shuffles" is a
        // separate instruction that still does — and the library that shuffles is the one that was
        // to be searched, not the searcher's own.
        if (!librarySearchSupport.checkSearchRestriction(gameData, controllerId)) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameLogService.append(gameData, GameLog.text(targetName + "'s library is shuffled."));
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " searches " + targetName
                    + "'s library but it is empty. Library is shuffled."));
            return;
        }

        CardPredicate filter = e.filter();
        List<Card> candidates = filter == null
                ? new ArrayList<>(deck)
                : new ArrayList<>(deck.stream()
                        .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, null))
                        .toList());
        if (candidates.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameLogService.append(gameData, GameLog.text(controllerName + " searches " + targetName
                    + "'s library but finds no matching cards. Library is shuffled."));
            return;
        }

        int count = Math.min(candidates.size(), amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, null)));
        if (count <= 0) {
            // "up to X" with X == 0 (e.g. Nightmare Incursion with no Swamps): nothing is found,
            // but the targeted player still shuffles.
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameLogService.append(gameData, GameLog.text(controllerName + " searches " + targetName
                    + "'s library for no cards. Library is shuffled."));
            return;
        }

        LibrarySearchParams.Builder params = LibrarySearchParams.builder(controllerId, candidates)
                .targetPlayerId(targetPlayerId)
                .remainingCount(count)
                .canFailToFind(e.canFailToFind())
                .destination(e.destination())
                .filterPredicate(filter);
        if (grantsPlayPermission(e.destination())) {
            // Grinning Totem reads the source card back out of the params when it queues
            // ExileToOwnerGraveyardAtNextUpkeep for the card left unplayed.
            params.sourceCards(List.of(entry.getCard()));
        }

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, params.build(),
                prompt(e.destination(), filter, targetName, count), e.canFailToFind(),
                controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library for {} card(s) to {} ({} candidates)",
                gameData.id, controllerName, targetName, count, e.destination(), candidates.size());
    }

    private static boolean grantsPlayPermission(LibrarySearchDestination destination) {
        return destination == LibrarySearchDestination.EXILE_PLAYABLE
                || destination == LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP;
    }

    private static String prompt(LibrarySearchDestination destination, CardPredicate filter,
                                 String targetName, int count) {
        String subject = "Search " + targetName + "'s library for a " + CardPredicateUtils.describeFilter(filter);
        return switch (destination) {
            case EXILE -> subject + " to exile (" + count + " remaining).";
            case EXILE_PLAYABLE, EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP -> filter == null
                    ? subject + " to exile face down."
                    : subject + " to exile.";
            case GRAVEYARD -> subject + " to put into their graveyard (" + count + " remaining).";
            case BATTLEFIELD_UNDER_SEARCHER -> subject + " to put onto the battlefield under your control.";
            default -> throw new IllegalStateException("Unsupported destination " + destination);
        };
    }
}
