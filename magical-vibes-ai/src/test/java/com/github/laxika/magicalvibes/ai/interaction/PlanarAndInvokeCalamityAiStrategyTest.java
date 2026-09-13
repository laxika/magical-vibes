package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PlanarAndInvokeCalamityAiStrategyTest {

    private final UUID playerId = UUID.randomUUID();
    private final GameData gameData = new GameData(UUID.randomUUID(), "test", playerId, "AI");
    private final AiGameActions actions = mock(AiGameActions.class);
    private final AiInteractionContext context =
            new AiInteractionContext(gameData, gameData.id, playerId, null, actions);

    @Test
    void planarChoiceSelectsOnlyOneEligiblePlane() throws Exception {
        Card excluded = new Card();
        Card first = new Card();
        Card second = new Card();
        answer(new PendingInteraction.PlanarCardChoice(playerId, List.of(excluded, first, second),
                List.of(first.getId(), second.getId()), "Choose a plane"));

        verify(actions).answerInteraction(new InteractionAnswer.CardsChosen(List.of(first.getId())));
    }

    @Test
    void spatialMergingOrdersEveryBottomedCard() throws Exception {
        answer(new PendingInteraction.SpatialMergingCardOrder(playerId, List.of(new Card()),
                List.of(new Card(), new Card(), new Card()), "Order cards"));

        verify(actions).answerInteraction(new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
    }

    @Test
    void invokeCalamityRespectsBudgetAndEligibilityAcrossBothZones() throws Exception {
        Card excluded = card(6);
        Card expensive = card(4);
        Card overBudget = card(3);
        Card cheap = card(2);
        Card free = card(0);
        gameData.playerHands.put(playerId, List.of(excluded, expensive, free));
        gameData.playerGraveyards.put(playerId, List.of(overBudget, cheap));

        answer(new PendingInteraction.InvokeCalamityCastChoice(playerId,
                List.of(expensive.getId(), overBudget.getId(), cheap.getId(), free.getId())));

        verify(actions).answerInteraction(
                new InteractionAnswer.CardsChosen(List.of(expensive.getId(), cheap.getId())));
    }

    @Test
    void invokeCalamityCanChooseNoSpells() throws Exception {
        answer(new PendingInteraction.InvokeCalamityCastChoice(playerId, List.of()));

        verify(actions).answerInteraction(new InteractionAnswer.CardsChosen(List.of()));
    }

    @Test
    void strategiesDoNotAnswerForAnotherPlayer() throws Exception {
        UUID otherPlayerId = UUID.randomUUID();
        answer(new PendingInteraction.PlanarCardChoice(otherPlayerId, List.of(), List.of(), "Choose"));
        answer(new PendingInteraction.SpatialMergingCardOrder(otherPlayerId, List.of(), List.of(), "Order"));
        answer(new PendingInteraction.InvokeCalamityCastChoice(otherPlayerId, List.of()));

        verifyNoInteractions(actions);
    }

    private void answer(PendingInteraction interaction) throws Exception {
        AiInteractionStrategies.forInteraction(interaction).answer(interaction, context);
    }

    private Card card(int manaValue) {
        Card card = new Card();
        card.setManaCost("{" + manaValue + "}");
        return card;
    }
}
