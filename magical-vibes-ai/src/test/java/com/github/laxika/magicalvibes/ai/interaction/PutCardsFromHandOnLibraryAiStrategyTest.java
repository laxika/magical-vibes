package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PutCardsFromHandOnLibraryAiStrategyTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AiGameActions gameActions;
    @Mock
    private Connection selfConnection;

    private GameData gameData;
    private UUID aiPlayerId;
    private UUID opponentId;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        opponentId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", aiPlayerId, "AI");
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(gameData, gameData.id, aiPlayerId,
                gameQueryService, gameActions, selfConnection);
    }

    private static Card spell(String name, String manaCost) {
        Card c = new Card();
        c.setName(name);
        c.setType(CardType.SORCERY);
        c.setManaCost(manaCost);
        return c;
    }

    @Nested
    @DisplayName("PutCardsFromHandOnLibraryCardChoiceAiStrategy")
    class CardChoice {

        private final PutCardsFromHandOnLibraryCardChoiceAiStrategy strategy =
                new PutCardsFromHandOnLibraryCardChoiceAiStrategy();

        private PendingInteraction.PutCardsFromHandOnLibraryCardChoice choice(
                UUID playerId, List<Card> cards, int maxCount) {
            return new PendingInteraction.PutCardsFromHandOnLibraryCardChoice(
                    playerId, cards.stream().map(Card::getId).toList(), cards, maxCount, false);
        }

        @Test
        @DisplayName("Registered in AiInteractionStrategies")
        void registeredInStrategies() {
            assertThat(AiInteractionStrategies.forInteraction(choice(aiPlayerId, List.of(), 2)))
                    .isInstanceOf(PutCardsFromHandOnLibraryCardChoiceAiStrategy.class);
        }

        @Test
        @DisplayName("Wrong deciding player: does not answer")
        void ignoresWrongPlayer() throws Exception {
            strategy.answer(choice(opponentId, List.of(spell("Bolt", "{R}")), 1), context());

            verify(gameActions, never()).answerInteraction(eq(selfConnection), any());
        }

        @Test
        @DisplayName("Picks the highest-mana-value cards up to maxCount")
        void picksHighestManaValueCards() throws Exception {
            Card cheap = spell("Cheap", "{1}");
            Card mid = spell("Mid", "{2}{U}");
            Card expensive = spell("Expensive", "{4}{U}{U}");

            strategy.answer(choice(aiPlayerId, List.of(cheap, expensive, mid), 2), context());

            ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
            verify(gameActions).answerInteraction(eq(selfConnection), captor.capture());
            assertThat(((InteractionAnswer.CardsChosen) captor.getValue()).cardIds())
                    .containsExactly(expensive.getId(), mid.getId());
        }

        @Test
        @DisplayName("Only picks cards in validCardIds")
        void respectsValidCardIds() throws Exception {
            Card valid = spell("Valid", "{1}");
            Card invalid = spell("Invalid But Expensive", "{6}");
            var interaction = new PendingInteraction.PutCardsFromHandOnLibraryCardChoice(
                    aiPlayerId, List.of(valid.getId()), List.of(valid, invalid), 2, false);

            strategy.answer(interaction, context());

            ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
            verify(gameActions).answerInteraction(eq(selfConnection), captor.capture());
            assertThat(((InteractionAnswer.CardsChosen) captor.getValue()).cardIds())
                    .containsExactly(valid.getId());
        }

        @Test
        @DisplayName("Answers an empty pick instead of hanging when nothing is selectable")
        void answersEmptyPickWhenNothingSelectable() throws Exception {
            strategy.answer(choice(aiPlayerId, List.of(), 2), context());

            ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
            verify(gameActions).answerInteraction(eq(selfConnection), captor.capture());
            assertThat(((InteractionAnswer.CardsChosen) captor.getValue()).cardIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PutCardsFromHandOnLibraryDestinationChoiceAiStrategy")
    class DestinationChoice {

        private final PutCardsFromHandOnLibraryDestinationChoiceAiStrategy strategy =
                new PutCardsFromHandOnLibraryDestinationChoiceAiStrategy();

        @Test
        @DisplayName("Registered in AiInteractionStrategies")
        void registeredInStrategies() {
            var interaction = new PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice(
                    aiPlayerId, List.of(UUID.randomUUID()));
            assertThat(AiInteractionStrategies.forInteraction(interaction))
                    .isInstanceOf(PutCardsFromHandOnLibraryDestinationChoiceAiStrategy.class);
        }

        @Test
        @DisplayName("Wrong deciding player: does not answer")
        void ignoresWrongPlayer() throws Exception {
            strategy.answer(new PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice(
                    opponentId, List.of(UUID.randomUUID())), context());

            verify(gameActions, never()).answerInteraction(eq(selfConnection), any());
        }

        @Test
        @DisplayName("Chooses the bottom of the library")
        void choosesBottom() throws Exception {
            strategy.answer(new PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice(
                    aiPlayerId, List.of(UUID.randomUUID())), context());

            ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
            verify(gameActions).answerInteraction(eq(selfConnection), captor.capture());
            assertThat(((InteractionAnswer.ListChoiceMade) captor.getValue()).choice()).isEqualTo("Bottom");
        }
    }
}
