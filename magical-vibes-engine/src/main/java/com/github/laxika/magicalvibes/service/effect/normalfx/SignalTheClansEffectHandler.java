package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SignalTheClansEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SignalTheClansEffect}: the controller searches their library for three creature
 * cards and reveals them. The picks are held in the
 * {@link LibrarySearchDestination#SIGNAL_THE_CLANS_POOL} pool; the random-card-to-hand decision is
 * made once the search ends ({@code LibraryChoiceHandlerService}), because it depends on how many
 * distinct names were actually revealed.
 *
 * <p>A library with no creature cards leaves nothing to reveal, so it just shuffles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignalTheClansEffectHandler implements NormalEffectHandlerBean {

    private static final int SEARCH_COUNT = 3;

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SignalTheClansEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> creatures = deck == null ? List.of()
                : deck.stream().filter(card -> card.hasType(CardType.CREATURE)).toList();

        if (creatures.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but finds no creature cards. Library is shuffled."));
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(creatures))
                        .remainingCount(SEARCH_COUNT)
                        .reveals(true)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.SIGNAL_THE_CLANS_POOL)
                        .build(),
                "Search your library for a creature card to reveal (" + SEARCH_COUNT + " remaining).", true);

        log.info("Game {} - {} begins a Signal the Clans search", gameData.id, playerName);
    }
}
