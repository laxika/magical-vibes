package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * An "and/or" search's zones: yes shows the search with the library's cards, and the library counts as searched
 * whatever is found; no shows it without them, and the library is neither searched nor shuffled (the Dark Supplicant
 * rulings). A search left with no card to offer ends at once, finding nothing.
 */
@Component
@RequiredArgsConstructor
public class AndOrSearchZonesChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AndOrSearchZonesChoice> {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SearchLibraryAndOrGraveyardChoiceInteractionHandler searchChoiceHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<PendingInteraction.AndOrSearchZonesChoice> handledType() {
        return PendingInteraction.AndOrSearchZonesChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.AndOrSearchZonesChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.decidingPlayerId())) {
            throw new IllegalStateException("Not your choice of zones");
        }
        boolean searchesLibrary = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        PendingInteraction.SearchLibraryAndOrGraveyardChoice search = searchesLibrary
                ? interaction.search().withLibrarySearched()
                : interaction.search().withoutLibrary();
        String playerName = gameData.playerIdToName.get(search.playerId());
        gameLogService.append(gameData, GameLog.text(searchesLibrary
                ? playerName + " searches their library too."
                : playerName + " chooses not to search their library."));
        gameData.interaction.clearAwaitingInput();
        if (search.pool().isEmpty()) {
            searchChoiceHandler.complete(gameData, search, List.of());
            return;
        }
        interactionHandlerRegistry.begin(gameData, search);
    }
}
