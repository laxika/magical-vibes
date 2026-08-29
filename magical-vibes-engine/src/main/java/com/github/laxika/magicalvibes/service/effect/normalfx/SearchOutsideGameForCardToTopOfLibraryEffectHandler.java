package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchOutsideGameForCardToTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchOutsideGameForCardToTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> sideboard = gameData.playerSideboards.getOrDefault(controllerId, List.of());
        if (sideboard.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no card outside the game."));
            return;
        }

        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(sideboard))
                .reveals(false)
                .canFailToFind(true)
                .remainingCount(1)
                .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                .shuffleAfterSelection(false)
                .sourceSideboard(true)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                params,
                "You may put a card you own from outside the game on top of your library.",
                true));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " searches outside the game for a card."));
    }
}
