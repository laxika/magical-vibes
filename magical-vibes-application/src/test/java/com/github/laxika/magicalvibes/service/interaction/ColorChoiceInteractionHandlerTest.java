package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ColorChoiceInteractionHandlerTest {

    @Mock private SessionManager sessionManager;
    @Mock private ChoiceHandlerService choiceHandlerService;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new ColorChoiceInteractionHandler(sessionManager, choiceHandlerService));

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
        registry.begin(gd, manaColorChoice());

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
        ChooseFromListMessage msg = (ChooseFromListMessage) messageCaptor.getValue();
        assertThat(msg.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(msg.prompt()).isEqualTo("Choose a color of mana to add.");
    }

    @Test
    @DisplayName("dispatchAnswer delegates the chosen value to ChoiceHandlerService")
    void dispatchDelegates() {
        registry.begin(gd, manaColorChoice());
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.ListChoiceMade("RED"));

        assertThat(handled).isTrue();
        verify(choiceHandlerService).handleListChoice(gd, player, "RED");
    }

    @Test
    @DisplayName("replayPrompt re-sends only to the decider")
    void replayOnlyToDecider() {
        registry.begin(gd, manaColorChoice());
        org.mockito.Mockito.clearInvocations(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
        verifyNoInteractions(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseFromListMessage.class));
    }
}
