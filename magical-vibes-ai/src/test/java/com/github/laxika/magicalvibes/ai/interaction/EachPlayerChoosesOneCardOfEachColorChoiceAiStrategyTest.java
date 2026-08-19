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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EachPlayerChoosesOneCardOfEachColorChoiceAiStrategyTest {

    private final EachPlayerChoosesOneCardOfEachColorChoiceAiStrategy strategy =
            new EachPlayerChoosesOneCardOfEachColorChoiceAiStrategy();

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
    void choosesHighestManaValueCardNotAlreadyPreserved() throws Exception {
        Card alreadyChosen = card("Already chosen", "{8}");
        Card cheap = card("Cheap", "{2}");
        Card expensive = card("Expensive", "{5}");
        gameData.playerHands.put(aiPlayerId, List.of(alreadyChosen, cheap, expensive));

        PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice interaction =
                new PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice(
                        aiPlayerId, List.of(0, 1, 2), List.of(aiPlayerId), 0, 1,
                        List.of(alreadyChosen.getId()), Map.of(), aiPlayerId, "Noxious Vapors");

        strategy.answer(interaction,
                new AiInteractionContext(gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        verify(gameActions).answerInteraction(new InteractionAnswer.CardIndexChosen(2));
    }

    private Card card(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost(manaCost);
        return card;
    }
}
