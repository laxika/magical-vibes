package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ColorChoiceInteractionHandlerTest {

    @Mock private ChoiceHandlerService choiceHandlerService;

    private InteractionHandlerRegistry registry;
    private InteractionProjectionTestSupport projectionSupport;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectionSupport = new InteractionProjectionTestSupport();
        registry = projectionSupport.registry();
        projectionSupport.register(new ColorChoiceInteractionHandler(choiceHandlerService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
    }

    private PendingInteraction.ColorChoice manaColorChoice() {
        return new PendingInteraction.ColorChoice(
                PLAYER1_ID, null, null, new ChoiceContext.ManaColorChoice(PLAYER1_ID, false),
                List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"), "Choose a color of mana to add.");
    }

    @Test
    @DisplayName("begin sets COLOR_CHOICE state and sends the carried options and prompt")
    void beginSendsPrompt() {
        InteractionPromptMessage msg = projectionSupport.begin(gd, manaColorChoice());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(msg.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(msg.prompt()).isEqualTo("Choose a color of mana to add.");
        assertThat(msg.searchable()).isFalse();
    }

    @Test
    @DisplayName("card-name choice is flagged searchable so the client renders an autocomplete box")
    void cardNameChoiceIsSearchable() {
        PendingInteraction.ColorChoice choice = new PendingInteraction.ColorChoice(
                PLAYER1_ID, null, null, new ChoiceContext.CardNameChoice(null, PLAYER1_ID, List.of()),
                List.of("Grizzly Bears", "Llanowar Elves"), "Choose a nonland creature name.");

        InteractionPromptMessage msg = projectionSupport.begin(gd, choice);
        assertThat(msg.searchable()).isTrue();
    }

    @Test
    @DisplayName("dispatchAnswer delegates the chosen value to ChoiceHandlerService")
    void dispatchDelegates() {
        projectionSupport.begin(gd, manaColorChoice());
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.ListChoiceMade("RED"));

        assertThat(handled).isTrue();
        verify(choiceHandlerService).handleListChoice(gd, player, "RED");
    }

    @Test
    @DisplayName("replayPrompt re-sends only to the decider")
    void replayOnlyToDecider() {
        projectionSupport.begin(gd, manaColorChoice());

        assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
        assertThat(projectionSupport.projectedPrompt(gd))
                .isInstanceOf(InteractionPromptMessage.class);
    }
}
