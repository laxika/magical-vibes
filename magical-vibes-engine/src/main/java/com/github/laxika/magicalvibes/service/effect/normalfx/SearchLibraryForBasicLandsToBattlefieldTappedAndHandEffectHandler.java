package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> basicLands = deck.stream()
                .filter(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC))
                .toList();

        if (basicLands.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        // First pick: basic land to battlefield tapped (no shuffle yet); the follow-up
        // hand search rides the search interaction
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(basicLands))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .shuffleAfterSelection(false)
                .followUp(LibrarySearchFollowUp.forBasicLandToHand())
                .build(), "Search your library for a basic land card to put onto the battlefield tapped.", true);

        log.info("Game {} - {} searches library for Cultivate ({} basic lands)", gameData.id, playerName, basicLands.size());
    }
}
