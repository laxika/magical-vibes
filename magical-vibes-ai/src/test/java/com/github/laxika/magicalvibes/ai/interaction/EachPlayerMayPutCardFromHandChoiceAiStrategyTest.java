package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EachPlayerMayPutCardFromHandChoiceAiStrategyTest {

    private final EachPlayerMayPutCardFromHandChoiceAiStrategy strategy =
            new EachPlayerMayPutCardFromHandChoiceAiStrategy();

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
    void choosesHighestManaValueEligibleCard() throws Exception {
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");
        Card ineligible = card("Ineligible", "{9}");
        gameData.playerHands.put(aiPlayerId, List.of(cheap, expensive, ineligible));

        strategy.answer(interaction(List.of(cheap.getId(), expensive.getId())), context());

        assertChosen(List.of(expensive.getId()));
    }

    @Test
    void declinesWhenNoEligibleCardIsInHand() throws Exception {
        gameData.playerHands.put(aiPlayerId, List.of(card("Ineligible", "{3}")));

        strategy.answer(interaction(List.of(UUID.randomUUID())), context());

        assertChosen(List.of());
    }

    private PendingInteraction.EachPlayerMayPutCardFromHandChoice interaction(List<UUID> validCardIds) {
        return new PendingInteraction.EachPlayerMayPutCardFromHandChoice(
                aiPlayerId, validCardIds, List.of(), List.of(), new CardTruePredicate(), "a permanent", "Show and Tell");
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(gameData, gameData.id, aiPlayerId, gameQueryService, gameActions);
    }

    private void assertChosen(List<UUID> expectedCardIds) throws Exception {
        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.CardsChosen(expectedCardIds));
    }

    private Card card(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost(manaCost);
        return card;
    }
}
