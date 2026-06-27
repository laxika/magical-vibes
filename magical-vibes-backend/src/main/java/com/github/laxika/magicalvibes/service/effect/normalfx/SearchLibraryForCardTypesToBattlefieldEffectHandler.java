package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
public class SearchLibraryForCardTypesToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardTypesToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCardTypesToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                       SearchLibraryForCardTypesToBattlefieldEffect effect) {
        if (effect.maxCount() > 1) {
            resolveMultiPickSearchToBattlefield(gameData, entry, effect);
            return;
        }

        String desc = CardPredicateUtils.describeFilter(effect.filter());
        String descPlural = desc.replace(" card", " cards");
        LibrarySearchDestination destination = effect.entersTapped()
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String prompt = effect.entersTapped()
                ? "Search your library for a " + desc + " and put it onto the battlefield tapped."
                : "Search your library for a " + desc + " and put it onto the battlefield.";

        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> gameQueryService.matchesCardPredicate(card, effect.filter(), null),
                descPlural,
                prompt,
                false,
                true,
                destination
        );
    }
    private void resolveMultiPickSearchToBattlefield(GameData gameData, StackEntry entry,
                                                      SearchLibraryForCardTypesToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String desc = CardPredicateUtils.describeFilter(effect.filter());
        String descPlural = desc.replace(" card", " cards");

        List<Card> matchingCards = deck.stream()
                .filter(card -> gameQueryService.matchesCardPredicate(card, effect.filter(), null))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no " + descPlural + ". Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        LibrarySearchDestination destination = effect.entersTapped()
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String destinationText = effect.entersTapped() ? "onto the battlefield tapped" : "onto the battlefield";
        String prompt = "Search your library for a " + desc + " to put " + destinationText
                + " (" + effect.maxCount() + " remaining).";

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .remainingCount(effect.maxCount())
                .canFailToFind(true)
                .destination(destination)
                .filterPredicate(effect.filter())
                .build(), prompt, true);

        log.info("Game {} - {} searches library for up to {} cards", gameData.id, playerName, effect.maxCount());
    }
}
