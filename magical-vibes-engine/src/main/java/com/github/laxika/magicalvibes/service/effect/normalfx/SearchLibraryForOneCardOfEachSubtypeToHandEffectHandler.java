package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeToHandEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameLogService;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForOneCardOfEachSubtypeToHandEffect} (Gem of Becoming): searches the
 * controller's library for one card of each listed subtype to hand, driven through the shared
 * {@link LibrarySearchSupport#startNextToHandPick} descriptor queue so that each subtype is a
 * separate revealed pick and the library is shuffled once after the last subtype.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForOneCardOfEachSubtypeToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForOneCardOfEachSubtypeToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForOneCardOfEachSubtypeToHandEffect searchEffect =
                (SearchLibraryForOneCardOfEachSubtypeToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        librarySearchSupport.startNextToHandPick(gameData, controllerId,
                LibrarySearchFollowUp.subtypeToHandPicks(searchEffect.subtypes()));
    }
}
