package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistantMemoriesEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DistantMemoriesEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistantMemoriesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry);
    }

    private void doResolve(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            // Empty library: no card to exile, but the "if no player does" clause still triggers — draw 3
            String logMsg = playerName + " searches their library but it is empty. " + playerName + " draws three cards.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            for (int i = 0; i < 3; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            return;
        }

        List<Card> allCards = new ArrayList<>(deck);

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, allCards)
                .destination(LibrarySearchDestination.EXILE)
                .followUp(LibrarySearchFollowUp.opponentExile(
                        new com.github.laxika.magicalvibes.model.PendingOpponentExileChoice(controllerId, 3)))
                .build(), "Search your library for a card to exile.", false);

        log.info("Game {} - {} searching library for Distant Memories ({} cards in library)", gameData.id, playerName, allCards.size());
    }
}
