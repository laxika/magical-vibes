package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class XValueChoiceInteractionHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TurnProgressionService turnProgressionService;
    @Mock private EffectResolutionService effectResolutionService;

    @InjectMocks
    private XValueChoiceInteractionHandler handler;

    private InteractionHandlerRegistry registry;
    private InteractionProjectionTestSupport projectionSupport;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectionSupport = new InteractionProjectionTestSupport();
        registry = projectionSupport.registry();
        projectionSupport.register(handler);

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
            projectionSupport.begin(gd, choice(5));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
            assertThat(gd.interaction.activeInteraction()).isEqualTo(choice(5));
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage with correct parameters")
        void sendsMessage() {
            InteractionPromptMessage msg = projectionSupport.begin(
                    gd,
                    new PendingInteraction.XValueChoice(
                            PLAYER1_ID, 10, "Choose X value", "Blaze"));
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

            projectionSupport.begin(gd, choice(5));
        }
    }

    @Nested
    @DisplayName("dispatchAnswer")
    class DispatchAnswer {

        @Test
        @DisplayName("Stores the chosen value and clears the awaiting state")
        void storesChosenValue() {
            projectionSupport.begin(gd, choice(5));

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
            projectionSupport.begin(gd, choice(5));

            assertThatThrownBy(() -> registry.dispatchAnswer(gd, new Player(PLAYER1_ID, "Player1"),
                    new InteractionAnswer.NumberChosen(6)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 0 and 5");
        }

        @Test
        @DisplayName("Rejects an answer from the wrong player")
        void rejectsWrongPlayer() {
            projectionSupport.begin(gd, choice(5));

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
            projectionSupport.begin(gd, choice(5));

            boolean handled = registry.replayPrompt(gd, PLAYER1_ID);

            assertThat(handled).isTrue();
            assertThat(projectionSupport.projectedPrompt(gd))
                    .isInstanceOf(InteractionPromptMessage.class);
        }

        @Test
        @DisplayName("Does not re-send to a reconnecting non-decider but still reports handled")
        void skipsNonDecider() {
            projectionSupport.begin(gd, choice(5));

            boolean handled = registry.replayPrompt(gd, PLAYER2_ID);

            assertThat(handled).isTrue();
        }
    }
}
