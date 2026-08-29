package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToBattlefieldOrHandByManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCardToBattlefieldOrHandByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardToBattlefieldOrHandByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCardToBattlefieldOrHandByManaValueEffect search =
                (SearchLibraryForCardToBattlefieldOrHandByManaValueEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        CardPredicate filter = search.filter();
        List<Card> matchingCards = deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no matching cards. Library is shuffled."));
            return;
        }

        String prompt = "Search your library for a " + CardPredicateUtils.describeFilter(filter) + " to reveal. Put it onto the battlefield "
                + "if its mana value is " + search.maxManaValue() + " or less. Otherwise, put it into your hand.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .reveals(true)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.HAND)
                        .filterPredicate(filter)
                        .battlefieldIfManaValueAtMost(search.maxManaValue())
                        .build(),
                prompt, true);
    }
}
