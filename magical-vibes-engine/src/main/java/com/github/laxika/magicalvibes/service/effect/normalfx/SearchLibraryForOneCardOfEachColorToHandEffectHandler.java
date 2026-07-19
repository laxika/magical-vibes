package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachColorToHandEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForOneCardOfEachColorToHandEffect} (Conflux): searches the
 * controller's library for one card of each colour to hand, driven through the shared
 * {@link LibrarySearchSupport#startNextColorToHandPick} colour queue so that each colour is a
 * separate revealed pick and the library is shuffled once after the last colour.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForOneCardOfEachColorToHandEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardColor> COLORS =
            List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForOneCardOfEachColorToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        librarySearchSupport.startNextColorToHandPick(gameData, controllerId,
                LibrarySearchFollowUp.colorToHandPicks(COLORS));
    }
}
