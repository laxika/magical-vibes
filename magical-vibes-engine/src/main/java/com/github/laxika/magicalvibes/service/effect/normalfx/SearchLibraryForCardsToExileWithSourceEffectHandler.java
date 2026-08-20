package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ON_ENTER_BATTLEFIELD: the controller searches their library for up to a configured number of
 * cards matching the effect's filter, exiling each one tracked "with" the source permanent, then
 * shuffles. The actual exile and shuffle are driven by
 * {@link com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService} via the
 * {@link LibrarySearchDestination#EXILE_WITH_SOURCE} destination.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardsToExileWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardsToExileWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCardsToExileWithSourceEffect searchEffect =
                (SearchLibraryForCardsToExileWithSourceEffect) effect;
        CardPredicate filter = searchEffect.filter();
        UUID controllerId = entry.getControllerId();

        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry, controllerId);
        if (sourcePermanentId == null) {
            log.info("Game {} - Source permanent no longer on battlefield, search-to-exile fizzles", gameData.id);
            return;
        }

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(c -> predicateEvaluationService.matchesCardPredicate(c, filter, null, gameData, controllerId))
                .toList();
        String desc = CardPredicateUtils.describeFilter(filter);

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no " + desc + ". Library is shuffled."));
            return;
        }

        int maxCount = Math.max(0, Math.min(searchEffect.maxCount(), matchingCards.size()));
        String prompt = searchEffect.maxCount() == Integer.MAX_VALUE
                ? "Search your library for a " + desc + " to exile (any number)."
                : "Search your library for up to " + maxCount + " " + desc + " to exile.";
        if (maxCount == 0) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(maxCount)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.EXILE_WITH_SOURCE)
                        .filterPredicate(filter)
                        .sourcePermanentId(sourcePermanentId)
                        .build(),
                prompt, true);

        log.info("Game {} - {} searches library to exile any number of {} ({} matches)",
                gameData.id, playerName, desc, matchingCards.size());
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry, UUID controllerId) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            return sourcePermanentId;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf != null) {
            for (Permanent p : bf) {
                if (p.getCard().getId().equals(entry.getCard().getId())) {
                    return p.getId();
                }
            }
        }
        return null;
    }
}
