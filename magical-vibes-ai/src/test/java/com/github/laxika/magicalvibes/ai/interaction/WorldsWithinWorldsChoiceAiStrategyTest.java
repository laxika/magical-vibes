package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class WorldsWithinWorldsChoiceAiStrategyTest {
    @Test
    void putsAllAvailableCreaturesOntoTheBattlefield() throws Exception {
        UUID playerId = UUID.randomUUID();
        List<UUID> creatures = List.of(UUID.randomUUID(), UUID.randomUUID());
        var actions = mock(AiGameActions.class);
        var game = new GameData(UUID.randomUUID(), "test", playerId, "AI");
        var choice = new PendingInteraction.WorldsWithinWorldsChoice(
                playerId, creatures, List.of(), creatures, Map.of(), "Worlds Within Worlds");

        new WorldsWithinWorldsChoiceAiStrategy().answer(choice,
                new AiInteractionContext(game, game.id, playerId, null, actions));

        verify(actions).answerInteraction(new InteractionAnswer.CardsChosen(creatures));
    }

    @Test
    void waitsForTheOtherPlayersChoice() throws Exception {
        UUID playerId = UUID.randomUUID();
        var actions = mock(AiGameActions.class);
        var game = new GameData(UUID.randomUUID(), "test", playerId, "AI");
        var choice = new PendingInteraction.WorldsWithinWorldsChoice(
                UUID.randomUUID(), List.of(), List.of(), List.of(), Map.of(), "Worlds Within Worlds");

        new WorldsWithinWorldsChoiceAiStrategy().answer(choice,
                new AiInteractionContext(game, game.id, playerId, null, actions));

        verifyNoInteractions(actions);
    }
}
