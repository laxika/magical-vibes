package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ColorChoiceAiStrategyTest {

    private final ColorChoiceAiStrategy strategy = new ColorChoiceAiStrategy();

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
    @DisplayName("Spell creature type choice answers with an offered creature type")
    void answersSpellCreatureTypeChoiceWithOfferedSubtype() throws Exception {
        PendingInteraction.ColorChoice interaction = new PendingInteraction.ColorChoice(
                aiPlayerId,
                null,
                null,
                new ChoiceContext.SpellCreatureTypeChoice(aiPlayerId),
                List.of("ELF", "WIZARD"),
                "Choose a creature type.");

        strategy.answer(interaction, new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions));

        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new InteractionAnswer.ListChoiceMade("ELF"));
    }
}
