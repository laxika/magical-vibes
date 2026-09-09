package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class LibrarySearchDestinationChoiceAiStrategyTest {

    private final UUID playerId = UUID.randomUUID();
    private final GameData gameData = new GameData(UUID.randomUUID(), "test", playerId, "AI");
    private final AiGameActions actions = mock(AiGameActions.class);
    private final AiInteractionContext context = new AiInteractionContext(
            gameData, gameData.id, playerId, null, actions);

    @Test
    void registeredStrategyPutsSearchedCardIntoHand() throws Exception {
        var choice = new PendingInteraction.LibrarySearchDestinationChoice(playerId, new Card());

        AiInteractionStrategies.forInteraction(choice).answer(choice, context);

        verify(actions).answerInteraction(new InteractionAnswer.ListChoiceMade("Hand"));
    }

    @Test
    void doesNotAnswerForAnotherPlayer() throws Exception {
        var choice = new PendingInteraction.LibrarySearchDestinationChoice(UUID.randomUUID(), new Card());

        AiInteractionStrategies.forInteraction(choice).answer(choice, context);

        verifyNoInteractions(actions);
    }
}
