package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardsToExileEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

/**
 * Resolves Jester's Cap: the controller searches target player's library for up to N cards,
 * exiles them, then that player shuffles. The per-card exile loop and shuffle are driven by
 * {@link com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService} via the
 * {@link LibrarySearchDestination#EXILE} destination with a {@code remainingCount}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchTargetLibraryForCardsToExileEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetLibraryForCardsToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((SearchTargetLibraryForCardsToExileEffect) effect).count();
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int effectiveCount = Math.min(count, deck.size());
        String prompt = "Search " + targetName + "'s library for a card to exile (" + effectiveCount + " remaining).";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(deck))
                .targetPlayerId(targetPlayerId)
                .remainingCount(effectiveCount)
                .destination(LibrarySearchDestination.EXILE)
                .build(), prompt, false, controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library to exile {} cards ({} in library)",
                gameData.id, controllerName, targetName, effectiveCount, deck.size());
    }
}
