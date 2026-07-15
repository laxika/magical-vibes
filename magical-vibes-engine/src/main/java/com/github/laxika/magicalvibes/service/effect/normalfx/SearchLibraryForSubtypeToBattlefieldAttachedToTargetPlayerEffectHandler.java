package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                                           SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        CardSubtype requiredSubtype = effect.requiredSubtype();
        String subtypeName = requiredSubtype.getDisplayName();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> gameQueryService.cardHasSubtype(card, requiredSubtype, gameData, controllerId))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no " + subtypeName + " cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} searches library, no {} cards found", gameData.id, playerName, subtypeName);
            return;
        }

        String prompt = "Search your library for a " + subtypeName + " card and put it onto the battlefield.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PLAYER)
                .attachToPlayerId(targetPlayerId)
                .build(), prompt, true);

        log.info("Game {} - {} searches library for {} card ({} matches)", gameData.id, playerName, subtypeName, matchingCards.size());
    }
}
