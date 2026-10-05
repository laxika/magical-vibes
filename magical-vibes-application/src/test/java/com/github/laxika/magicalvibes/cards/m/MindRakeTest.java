package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindRake.class, MotherBear.class})
class MindRakeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player chooses two cards to discard")
    void targetPlayerDiscardsTwoCards() {
        harness.setHand(player2, List.of(new MotherBear(), new MotherBear(), new MotherBear()));
        harness.setHand(player1, List.of(new MindRake()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Overload makes each player discard two cards")
    void overloadMakesEachPlayerDiscardTwoCards() {
        harness.setHand(player1, List.of(new MindRake(), new MotherBear(), new MotherBear()));
        harness.setHand(player2, List.of(new MotherBear(), new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        discardTwoFromCurrentChooser();
        discardTwoFromCurrentChooser();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Overload requires its alternate cost")
    void overloadRequiresItsAlternateCost() {
        harness.setHand(player1, List.of(new MindRake()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new MotherBear());
        harness.setHand(player1, List.of(new MindRake()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Mother Bear")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new MindRake(), new MotherBear(), new MotherBear()));
        harness.setHand(player2, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void targetWithOneCardDiscardsOnlyThatCard() {
        harness.setHand(player1, List.of(new MindRake()));
        harness.setHand(player2, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void targetWithEmptyHandDoesNotNeedAChoice() {
        harness.setHand(player1, List.of(new MindRake()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Mind Rake");
    }

    @Test
    void overloadSkipsEmptyControllerHandAndDiscardsOpponentsOnlyCard() {
        harness.setHand(player1, List.of(new MindRake()));
        harness.setHand(player2, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Mind Rake");
    }

    @Test
    void overloadKeepsChosenCardsHiddenUntilBothPlayersHaveChosen() {
        harness.setHand(player1, List.of(new MindRake(), new MotherBear(), new MotherBear()));
        harness.setHand(player2, List.of(new MotherBear(), new MotherBear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Mother Bear");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void discardTwoFromCurrentChooser() {
        Player chooser = ((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()
                .equals(player1.getId()) ? player1 : player2;
        harness.handleCardChosen(chooser, 0);
        harness.handleCardChosen(chooser, 0);
    }
}
