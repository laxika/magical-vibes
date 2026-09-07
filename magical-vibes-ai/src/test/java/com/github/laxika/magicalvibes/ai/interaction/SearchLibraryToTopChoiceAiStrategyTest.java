package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchLibraryToTopChoiceAiStrategyTest {

    private final SearchLibraryToTopChoiceAiStrategy strategy =
            new SearchLibraryToTopChoiceAiStrategy();

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
    void optionalSearchDoesNotChooseMoreThanTheMaximum() throws Exception {
        List<Card> pool = cards(9);

        strategy.answer(new PendingInteraction.SearchLibraryToTopChoice(
                aiPlayerId, pool, "creature", -1, 3, true), context());

        assertChosen(pool.subList(0, 3));
    }

    @Test
    void exactSearchChoosesTheRequiredNumber() throws Exception {
        List<Card> pool = cards(4);

        strategy.answer(new PendingInteraction.SearchLibraryToTopChoice(
                aiPlayerId, pool, "card", 2, pool.size(), false), context());

        assertChosen(pool.subList(0, 2));
    }

    @Test
    void unrestrictedSearchChoosesEveryMatchingCard() throws Exception {
        List<Card> pool = cards(4);

        strategy.answer(new PendingInteraction.SearchLibraryToTopChoice(
                aiPlayerId, pool, "Goblin"), context());

        assertChosen(pool);
    }

    @Test
    void ignoresAnotherPlayersChoice() throws Exception {
        strategy.answer(new PendingInteraction.SearchLibraryToTopChoice(
                UUID.randomUUID(), cards(1), "creature"), context());

        verify(gameActions, never()).answerInteraction(any());
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions);
    }

    private void assertChosen(List<Card> expected) throws Exception {
        ArgumentCaptor<InteractionAnswer> answer = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(answer.capture());
        assertThat(answer.getValue()).isEqualTo(new InteractionAnswer.CardsChosen(
                expected.stream().map(Card::getId).toList()));
    }

    private static List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> new Card())
                .toList();
    }
}
