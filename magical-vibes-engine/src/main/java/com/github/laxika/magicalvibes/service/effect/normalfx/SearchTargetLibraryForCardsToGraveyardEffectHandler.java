package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardsToGraveyardEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTargetLibraryForCardsToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetLibraryForCardsToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchTargetLibraryForCardsToGraveyardEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                       SearchTargetLibraryForCardsToGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> librarySearchSupport.matchesCardTypes(card, effect.cardTypes()))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            String logMsg = controllerName + " searches " + targetName + "'s library but finds no matching cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        String prompt = "Search " + targetName + "'s library for a creature card to put into their graveyard (" + effect.maxCount() + " remaining).";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .targetPlayerId(targetPlayerId)
                .remainingCount(effect.maxCount())
                .canFailToFind(true)
                .destination(LibrarySearchDestination.GRAVEYARD)
                .filterCardTypes(effect.cardTypes())
                .build(), prompt, true, controllerName + " searches " + targetName + "'s library.");
    }
}
