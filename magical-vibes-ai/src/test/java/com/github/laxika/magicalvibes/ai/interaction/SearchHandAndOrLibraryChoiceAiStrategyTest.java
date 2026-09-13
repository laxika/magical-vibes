package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SearchHandAndOrLibraryChoiceAiStrategyTest {
    private final UUID playerId = UUID.randomUUID();
    private final GameData gameData = new GameData(UUID.randomUUID(), "test", playerId, "AI");
    private final AiGameActions actions = mock(AiGameActions.class);
    private final AiInteractionContext context =
            new AiInteractionContext(gameData, gameData.id, playerId, null, actions);
    private final SearchHandAndOrLibraryChoiceAiStrategy strategy =
            new SearchHandAndOrLibraryChoiceAiStrategy();

    @Test
    void choosesOneCardFromTheCombinedSearchPool() throws Exception {
        Card small = new Card();
        small.setManaCost("{1}");
        Card large = new Card();
        large.setManaCost("{5}");
        strategy.answer(new PendingInteraction.SearchHandAndOrLibraryChoice(playerId,
                List.of(small, large), Set.of(small.getId()), Set.of(large.getId()), true,
                "creature", LibrarySearchDestination.BATTLEFIELD), context);
        verify(actions).answerInteraction(new InteractionAnswer.CardsChosen(List.of(large.getId())));
    }

    @Test
    void finishesAnEmptySearch() throws Exception {
        strategy.answer(choice(playerId), context);
        verify(actions).answerInteraction(new InteractionAnswer.CardsChosen(List.of()));
    }

    @Test
    void leavesAnotherPlayersChoiceAlone() throws Exception {
        strategy.answer(choice(UUID.randomUUID()), context);
        verifyNoInteractions(actions);
    }

    private PendingInteraction.SearchHandAndOrLibraryChoice choice(UUID decidingPlayerId) {
        return new PendingInteraction.SearchHandAndOrLibraryChoice(decidingPlayerId,
                List.of(), Set.of(), Set.of(), true, "creature", LibrarySearchDestination.BATTLEFIELD);
    }
}
