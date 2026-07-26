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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SpecializedInteractionAiStrategiesTest {

    private UUID aiPlayerId;
    private GameData gameData;
    private AiGameActions actions;
    private Connection connection;
    private AiInteractionContext context;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "specialized-ai", aiPlayerId, "AI");
        gameData.playerHands.put(aiPlayerId, new ArrayList<>());
        gameData.playerDecks.put(aiPlayerId, new ArrayList<>());
        actions = mock(AiGameActions.class);
        connection = mock(Connection.class);
        context = new AiInteractionContext(
                gameData,
                gameData.id,
                aiPlayerId,
                mock(GameQueryService.class),
                actions,
                connection);
    }

    @Test
    void adNauseamDeclinesWhenTheNextCardWouldBeLethal() throws Exception {
        gameData.playerLifeTotals.put(aiPlayerId, 3);
        gameData.playerDecks.get(aiPlayerId).add(card("Lethal", "{3}"));

        new AdNauseamRepeatChoiceAiStrategy().answer(
                new PendingInteraction.AdNauseamRepeatChoice(aiPlayerId, "Ad Nauseam"), context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.MayAbilityChosen(false));
    }

    @Test
    void keepCardsInHandChoosesOnlyTheHighestValueLegalCards() throws Exception {
        Card cheap = card("Cheap", "{1}");
        Card expensive = card("Expensive", "{5}");
        Card invalid = card("Invalid", "{9}");
        gameData.playerHands.get(aiPlayerId).addAll(List.of(cheap, expensive, invalid));

        new KeepCardsInHandChoiceAiStrategy().answer(
                new PendingInteraction.KeepCardsInHandChoice(
                        aiPlayerId,
                        List.of(cheap.getId(), expensive.getId()),
                        1,
                        List.of(),
                        "Source"),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(expensive.getId())));
    }

    @Test
    void sylvanLibraryPutsBackEligibleCardsInsteadOfPayingLife() throws Exception {
        Card first = card("First", "{1}");
        Card second = card("Second", "{2}");
        gameData.playerHands.get(aiPlayerId).addAll(List.of(first, second));

        new SylvanLibraryChoiceAiStrategy().answer(
                new PendingInteraction.SylvanLibraryChoice(
                        aiPlayerId, List.of(first.getId(), second.getId()), 2),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(
                        List.of(first.getId(), second.getId())));
    }

    @Test
    void brilliantUltimatumPileStrategiesProduceLegalAnswers() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();

        new BrilliantUltimatumPileSeparationChoiceAiStrategy().answer(
                new PendingInteraction.BrilliantUltimatumPileSeparationChoice(
                        aiPlayerId, List.of(first, second, third)),
                context);

        assertThat(capturedAnswer())
                .isEqualTo(new InteractionAnswer.CardsChosen(List.of(first)));
    }

    @Test
    void allNewSpecializedTypesAreRegistered() {
        assertThat(AiInteractionStrategies.registeredTypes()).contains(
                PendingInteraction.BrilliantUltimatumPileSeparationChoice.class,
                PendingInteraction.BrilliantUltimatumPileChoice.class,
                PendingInteraction.KeepCardsInHandChoice.class,
                PendingInteraction.SylvanLibraryChoice.class,
                PendingInteraction.AdNauseamRepeatChoice.class);
    }

    private InteractionAnswer capturedAnswer() throws Exception {
        ArgumentCaptor<InteractionAnswer> answer = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(actions).answerInteraction(eq(connection), answer.capture());
        return answer.getValue();
    }

    private static Card card(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost(manaCost);
        return card;
    }
}
