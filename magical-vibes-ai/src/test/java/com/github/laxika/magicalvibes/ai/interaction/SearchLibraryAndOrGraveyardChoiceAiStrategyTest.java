package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchLibraryAndOrGraveyardChoiceAiStrategyTest {

    private final SearchLibraryAndOrGraveyardChoiceAiStrategy strategy =
            new SearchLibraryAndOrGraveyardChoiceAiStrategy();

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AiGameActions gameActions;

    private GameData gameData;
    private UUID aiPlayerId;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", aiPlayerId, "AI");
    }

    @Test
    @DisplayName("An AI that controls Opposition Agent answers the search of the opponent it controls")
    void answersTheSearchItControlsThroughOppositionAgent() throws Exception {
        Card card = new Card();

        strategy.answer(choice(UUID.randomUUID(), aiPlayerId, card), context());

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(((InteractionAnswer.CardsChosen) captor.getValue()).cardIds()).containsExactly(card.getId());
    }

    @Test
    @DisplayName("An AI whose search an opponent's Opposition Agent controls doesn't answer it")
    void doesNotAnswerItsOwnSearchControlledByAnOpponent() throws Exception {
        strategy.answer(choice(aiPlayerId, UUID.randomUUID(), new Card()), context());

        verify(gameActions, never()).answerInteraction(any());
    }

    private PendingInteraction.SearchLibraryAndOrGraveyardChoice choice(UUID searcherId, UUID agentControllerId,
                                                                         Card card) {
        return new PendingInteraction.SearchLibraryAndOrGraveyardChoice(searcherId, List.of(card),
                Set.of(card.getId()), Set.of(), Set.of(), true, "card", LibrarySearchDestination.HAND, null, null, 0,
                1, false, true, agentControllerId);
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(gameData, gameData.id, aiPlayerId, gameQueryService, gameActions);
    }
}
