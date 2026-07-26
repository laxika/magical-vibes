package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
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
class GraveyardChoiceInteractionHandlersTest {

    @Mock private GraveyardChoiceHandlerService graveyardChoiceHandlerService;
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
        projectionSupport.register(
                new GraveyardChoiceInteractionHandler(graveyardChoiceHandlerService));
        projectionSupport.register(
                new GraveyardExileCostChoiceInteractionHandler(abilityActivationService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    @Nested
    @DisplayName("Graveyard choice")
    class Graveyard {

        @Test
        @DisplayName("begin sets GRAVEYARD_CHOICE state and sends the begin-time indices and prompt")
        void beginSendsPrompt() {
            projectionSupport.begin(gd, PendingInteraction.GraveyardChoice
                    .builder(PLAYER1_ID, List.of(0, 2), GraveyardChoiceDestination.HAND,
                            "Return a creature card from your graveyard to your hand.")
                    .build());

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
            InteractionPromptMessage msg = projectionSupport.projectedPrompt(gd);
            assertThat(msg.cardIndices()).containsExactly(0, 2);
            assertThat(msg.prompt()).isEqualTo("Return a creature card from your graveyard to your hand.");
            assertThat(msg.allGraveyards()).isFalse();
        }

        @Test
        @DisplayName("Sends allGraveyards=true when a card pool is carried")
        void allGraveyardsWithCardPool() {
            projectionSupport.begin(gd, PendingInteraction.GraveyardChoice
                    .builder(PLAYER1_ID, List.of(0), GraveyardChoiceDestination.BATTLEFIELD, "Choose.")
                    .cardPool(List.of(createCard("Grizzly Bears")))
                    .build());

            InteractionPromptMessage msg = projectionSupport.projectedPrompt(gd);
            assertThat(msg.allGraveyards()).isTrue();
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen index to GraveyardChoiceHandlerService")
        void dispatchDelegates() {
            projectionSupport.begin(gd, PendingInteraction.GraveyardChoice
                    .builder(PLAYER1_ID, List.of(0), GraveyardChoiceDestination.HAND, "Choose.")
                    .build());
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.GraveyardCardChosen(0));

            assertThat(handled).isTrue();
            verify(graveyardChoiceHandlerService).handleGraveyardCardChosen(gd, player, 0);
        }

    }

    @Nested
    @DisplayName("Graveyard exile cost choice")
    class ExileCost {

        @Test
        @DisplayName("begin sets ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE state and sends the cost prompt")
        void beginSendsPrompt() {
            projectionSupport.begin(gd, new PendingInteraction.GraveyardExileCostChoice(
                    PLAYER1_ID, List.of(0, 1),
                    "Choose a creature card from your graveyard to exile as an activation cost."));

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
            InteractionPromptMessage msg = projectionSupport.projectedPrompt(gd);
            assertThat(msg.cardIndices()).containsExactly(0, 1);
            assertThat(msg.prompt())
                    .isEqualTo("Choose a creature card from your graveyard to exile as an activation cost.");
            assertThat(msg.allGraveyards()).isFalse();
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen index to AbilityActivationService")
        void dispatchDelegates() {
            projectionSupport.begin(gd, new PendingInteraction.GraveyardExileCostChoice(
                    PLAYER1_ID, List.of(0), "Choose."));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.GraveyardCardChosen(0));

            assertThat(handled).isTrue();
            verify(abilityActivationService).handleActivatedAbilityGraveyardExileCostChosen(gd, player, 0);
        }

    }
}
