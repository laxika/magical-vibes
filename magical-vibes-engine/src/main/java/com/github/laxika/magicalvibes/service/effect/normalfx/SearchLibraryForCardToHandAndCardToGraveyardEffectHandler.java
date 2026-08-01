package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp.CardToGraveyardPick;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandAndCardToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForCardToHandAndCardToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardToHandAndCardToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCardToHandAndCardToGraveyardEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                           SearchLibraryForCardToHandAndCardToGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        CardPredicate filter = effect.filter();
        List<Card> candidates = filter == null
                ? new ArrayList<>(deck)
                : deck.stream()
                        .filter(c -> predicateEvaluationService.matchesCardPredicate(c, filter, null, gameData, controllerId))
                        .toList();

        if (candidates.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String noMatch = filter == null ? "cards" : "matching cards";
            String logMsg = playerName + " searches their library but finds no " + noMatch + ". Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        String prompt = filter == null
                ? "Search your library for a card to put into your hand."
                : "Search your library for a creature card to put into your hand.";
        CardToGraveyardPick graveyardPick = new CardToGraveyardPick(filter, effect.canFailToFind(), effect.reveals());

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, candidates)
                        .reveals(effect.reveals())
                        .canFailToFind(effect.canFailToFind())
                        .destination(LibrarySearchDestination.HAND)
                        .shuffleAfterSelection(false)
                        .filterPredicate(filter)
                        .followUp(LibrarySearchFollowUp.forCardToGraveyard(graveyardPick))
                        .build(),
                prompt, effect.canFailToFind());

        log.info("Game {} - {} searches library for card to hand then graveyard ({} candidates)",
                gameData.id, playerName, candidates.size());
    }
}
