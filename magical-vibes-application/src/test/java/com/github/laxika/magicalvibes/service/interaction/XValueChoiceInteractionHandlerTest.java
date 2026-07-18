package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class XValueChoiceInteractionHandlerTest {

    @Mock private SessionManager sessionManager;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TurnProgressionService turnProgressionService;
    @Mock private EffectResolutionService effectResolutionService;

    @InjectMocks
    private XValueChoiceInteractionHandler handler;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(handler);

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
    }

    private PendingInteraction.XValueChoice choice(int maxValue) {
        return new PendingInteraction.XValueChoice(PLAYER1_ID, maxValue, "Choose X", "Blaze");
    }

    @Nested
    @DisplayName("begin")
    class Begin {

        @Test
        @DisplayName("Sets the active interaction and the legacy X_VALUE_CHOICE state")
        void setsInteractionState() {
            registry.begin(gd, choice(5));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
            assertThat(gd.interaction.activeInteraction()).isEqualTo(choice(5));
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage with correct parameters")
        void sendsMessage() {
            registry.begin(gd, new PendingInteraction.XValueChoice(PLAYER1_ID, 10, "Choose X value", "Blaze"));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            InteractionPromptMessage msg = (InteractionPromptMessage) messageCaptor.getValue();
            assertThat(msg.maxCount()).isEqualTo(10);
            assertThat(msg.prompt()).isEqualTo("Choose X value");
            assertThat(msg.cardName()).isEqualTo("Blaze");
        }

        @Test
        @DisplayName("Redirects the prompt to the mind controller when the decider is mind-controlled")
        void redirectsToMindController() {
            UUID controllerId = UUID.randomUUID();
            gd.playerIdToName.put(controllerId, "Controller");
            gd.mindControlledPlayerId = PLAYER1_ID;
            gd.mindControllerPlayerId = controllerId;

            registry.begin(gd, choice(5));

            verify(sessionManager).sendToPlayer(eq(controllerId), messageCaptor.capture());
        }
    }

    @Nested
    @DisplayName("dispatchAnswer")
    class DispatchAnswer {

        @Test
        @DisplayName("Stores the chosen value and clears the awaiting state")
        void storesChosenValue() {
            registry.begin(gd, choice(5));

            boolean handled = registry.dispatchAnswer(gd, new Player(PLAYER1_ID, "Player1"),
                    new InteractionAnswer.NumberChosen(3));

            assertThat(handled).isTrue();
            assertThat(gd.chosenXValue).isEqualTo(3);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Rejects a value above the maximum")
        void rejectsValueAboveMax() {
            registry.begin(gd, choice(5));

            assertThatThrownBy(() -> registry.dispatchAnswer(gd, new Player(PLAYER1_ID, "Player1"),
                    new InteractionAnswer.NumberChosen(6)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 5");
        }

        @Test
        @DisplayName("Rejects an answer from the wrong player")
        void rejectsWrongPlayer() {
            registry.begin(gd, choice(5));

            assertThatThrownBy(() -> registry.dispatchAnswer(gd, new Player(PLAYER2_ID, "Player2"),
                    new InteractionAnswer.NumberChosen(2)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not your turn");
        }

        @Test
        @DisplayName("Returns false when no interaction is active")
        void returnsFalseWhenNothingActive() {
            boolean handled = registry.dispatchAnswer(gd, new Player(PLAYER1_ID, "Player1"),
                    new InteractionAnswer.NumberChosen(2));

            assertThat(handled).isFalse();
        }
    }

    @Nested
    @DisplayName("replayPrompt")
    class ReplayPrompt {

        @Test
        @DisplayName("Re-sends the prompt to the reconnecting decider")
        void resendsToDecider() {
            registry.begin(gd, choice(5));
            org.mockito.Mockito.clearInvocations(sessionManager);

            boolean handled = registry.replayPrompt(gd, PLAYER1_ID);

            assertThat(handled).isTrue();
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            assertThat(messageCaptor.getValue()).isInstanceOf(InteractionPromptMessage.class);
        }

        @Test
        @DisplayName("Does not re-send to a reconnecting non-decider but still reports handled")
        void skipsNonDecider() {
            registry.begin(gd, choice(5));
            org.mockito.Mockito.clearInvocations(sessionManager);

            boolean handled = registry.replayPrompt(gd, PLAYER2_ID);

            assertThat(handled).isTrue();
            verifyNoInteractions(sessionManager);
        }
    }
}
