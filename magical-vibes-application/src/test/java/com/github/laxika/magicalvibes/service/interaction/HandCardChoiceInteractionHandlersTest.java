package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HandCardChoiceInteractionHandlersTest {

    @Mock private CardChoiceHandlerService cardChoiceHandlerService;
    @Mock private AbilityActivationService abilityActivationService;

    private InteractionHandlerRegistry registry;
    private InteractionProjectionTestSupport projectionSupport;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectionSupport = new InteractionProjectionTestSupport();
        registry = projectionSupport.registry();
        projectionSupport.register(new HandCardChoiceInteractionHandlers.HandCardChoiceInteractionHandler(
                cardChoiceHandlerService));
        projectionSupport.register(new HandCardChoiceInteractionHandlers.TargetedHandCardChoiceInteractionHandler(
                cardChoiceHandlerService));
        projectionSupport.register(new HandCardChoiceInteractionHandlers.DiscardChoiceInteractionHandler(
                cardChoiceHandlerService));
        projectionSupport.register(new HandCardChoiceInteractionHandlers.ExileFromHandChoiceInteractionHandler(
                cardChoiceHandlerService));
        projectionSupport.register(new HandCardChoiceInteractionHandlers.ImprintFromHandChoiceInteractionHandler(
                cardChoiceHandlerService));
        projectionSupport.register(new HandCardChoiceInteractionHandlers.DiscardCostChoiceInteractionHandler(
                abilityActivationService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
    }

    private InteractionPromptMessage sentMessage() {
        return projectionSupport.projectedPrompt(gd);
    }

    @Nested
    @DisplayName("Plain and targeted hand card choices")
    class PlainAndTargeted {

        @Test
        @DisplayName("begin sets CARD_CHOICE and sends a declinable prompt")
        void beginPlain() {
            projectionSupport.begin(gd, new PendingInteraction.HandCardChoice(
                    PLAYER1_ID, List.of(0, 2), "Choose a creature card from your hand to put onto the battlefield."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
            InteractionPromptMessage msg = sentMessage();
            assertThat(msg.cardIndices()).containsExactly(0, 2);
            assertThat(msg.prompt()).isEqualTo("Choose a creature card from your hand to put onto the battlefield.");
            assertThat(msg.declinable()).isTrue();
        }

        @Test
        @DisplayName("begin sets TARGETED_CARD_CHOICE and sends a declinable prompt")
        void beginTargeted() {
            projectionSupport.begin(gd, new PendingInteraction.TargetedHandCardChoice(
                    PLAYER1_ID, List.of(1), UUID.randomUUID(), "Choose an Aura."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
            InteractionPromptMessage msg = sentMessage();
            assertThat(msg.declinable()).isTrue();
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen index to CardChoiceHandlerService")
        void dispatchDelegates() {
            projectionSupport.begin(
                    gd,
                    new PendingInteraction.HandCardChoice(
                            PLAYER1_ID, List.of(0), "Choose."));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.CardIndexChosen(0));

            assertThat(handled).isTrue();
            verify(cardChoiceHandlerService).handleHandCardChosen(gd, player, 0);
        }
    }

    @Nested
    @DisplayName("Discard, exile-from-hand, and imprint choices")
    class DiscardExileImprint {

        @Test
        @DisplayName("Discard begin sends a non-declinable prompt and dispatch delegates")
        void discard() {
            projectionSupport.begin(gd, new PendingInteraction.DiscardChoice(
                    PLAYER1_ID, List.of(0, 1), 2, DiscardFollowUp.NONE, "Choose a card to discard."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            InteractionPromptMessage msg = sentMessage();
            assertThat(msg.prompt()).isEqualTo("Choose a card to discard.");
            assertThat(msg.declinable()).isFalse();

            Player player = new Player(PLAYER1_ID, "Player1");
            assertThat(registry.dispatchAnswer(gd, player, new InteractionAnswer.CardIndexChosen(1))).isTrue();
            verify(cardChoiceHandlerService).handleDiscardCardChosen(gd, player, 1);
        }

        @Test
        @DisplayName("Exile-from-hand begin sends its prompt and dispatch delegates")
        void exileFromHand() {
            projectionSupport.begin(gd, new PendingInteraction.ExileFromHandChoice(
                    PLAYER1_ID, List.of(0), UUID.randomUUID(), null, 1, "Choose a card to exile."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
            assertThat(sentMessage().prompt()).isEqualTo("Choose a card to exile.");

            Player player = new Player(PLAYER1_ID, "Player1");
            assertThat(registry.dispatchAnswer(gd, player, new InteractionAnswer.CardIndexChosen(0))).isTrue();
            verify(cardChoiceHandlerService).handleExileFromHandChosen(gd, player, 0);
        }

        @Test
        @DisplayName("Imprint begin sends its prompt and dispatch delegates")
        void imprint() {
            projectionSupport.begin(gd, new PendingInteraction.ImprintFromHandChoice(
                    PLAYER1_ID, List.of(0, 1), UUID.randomUUID(), "Choose an artifact card from your hand to imprint."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
            assertThat(sentMessage().prompt()).isEqualTo("Choose an artifact card from your hand to imprint.");

            Player player = new Player(PLAYER1_ID, "Player1");
            assertThat(registry.dispatchAnswer(gd, player, new InteractionAnswer.CardIndexChosen(1))).isTrue();
            verify(cardChoiceHandlerService).handleImprintFromHandCardChosen(gd, player, 1);
        }
    }

    @Nested
    @DisplayName("Discard cost choice")
    class DiscardCost {

        @Test
        @DisplayName("begin sets ACTIVATED_ABILITY_DISCARD_COST_CHOICE and sends the cost prompt")
        void beginSendsPrompt() {
            projectionSupport.begin(gd, new PendingInteraction.DiscardCostChoice(
                    PLAYER1_ID, List.of(0), "Choose a land card to discard as an activation cost."));

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.DiscardCostChoice.class);
            InteractionPromptMessage msg = sentMessage();
            assertThat(msg.prompt()).isEqualTo("Choose a land card to discard as an activation cost.");
            assertThat(msg.declinable()).isFalse();
        }

        @Test
        @DisplayName("dispatchAnswer delegates to AbilityActivationService")
        void dispatchDelegates() {
            projectionSupport.begin(
                    gd,
                    new PendingInteraction.DiscardCostChoice(
                            PLAYER1_ID, List.of(0), "Choose."));
            Player player = new Player(PLAYER1_ID, "Player1");

            assertThat(registry.dispatchAnswer(gd, player, new InteractionAnswer.CardIndexChosen(0))).isTrue();
            verify(abilityActivationService).handleActivatedAbilityDiscardCostChosen(gd, player, 0);
        }
    }

}
