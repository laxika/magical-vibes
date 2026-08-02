package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftsUngivenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GiftsUngivenEffect}: the controller searches their library for up to four cards
 * with different names and reveals them, then the targeted opponent chooses two of them for the
 * graveyard while the rest go to the controller's hand.
 *
 * <p>The search runs with {@link LibrarySearchDestination#GIFTS_UNGIVEN_POOL}, which keeps the found
 * cards out of every zone until the opponent has decided ({@code LibraryChoiceHandlerService});
 * the chooser is carried on a {@link PendingPileSeparation} queued here with
 * {@link CardPileDisposition#GIFTS_UNGIVEN} so the opponent stays the targeted player rather than
 * being re-derived at completion time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GiftsUngivenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiftsUngivenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        UUID opponentId = entry.getTargetId();
        if (opponentId == null || opponentId.equals(controllerId)) return;

        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId, List.of(),
                List.of(), Map.of(), List.of(), List.of(), CardPileDisposition.GIFTS_UNGIVEN));

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(deck))
                        .remainingCount(4)
                        .reveals(true)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.GIFTS_UNGIVEN_POOL)
                        .requireDifferentNames(true)
                        .build(),
                "Search your library for a card with a different name to reveal (4 remaining).", true);

        log.info("Game {} - {} begins a Gifts Ungiven search", gameData.id, playerName);
    }
}
