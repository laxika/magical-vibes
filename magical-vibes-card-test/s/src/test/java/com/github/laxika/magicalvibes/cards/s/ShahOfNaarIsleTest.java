package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShahOfNaarIsle.class})
class ShahOfNaarIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Echo does not create an enters-the-battlefield trigger")
    void enteringDoesNotCreateATrigger() {
        harness.castFromHand(player1, new ShahOfNaarIsle(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shah of Naar Isle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying echo creates a separate draw trigger before the opponent chooses")
    void payingEchoCreatesSeparateDrawTrigger() {
        harness.setLibrary(player2, List.of(
                new ShahOfNaarIsle(), new ShahOfNaarIsle(), new ShahOfNaarIsle()));
        castShah();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Shah of Naar Isle");
    }

    @Test
    @DisplayName("An opponent may draw fewer than three cards after echo is paid")
    void opponentMayDrawOneCard() {
        harness.setLibrary(player2, List.of(
                new ShahOfNaarIsle(), new ShahOfNaarIsle(), new ShahOfNaarIsle()));
        castShah();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Shah of Naar Isle");
    }

    @Test
    @DisplayName("Echo is not paid again on the following upkeep")
    void echoDoesNotRepeatOnFollowingUpkeep() {
        castShah();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        advanceToUpkeep(player2);
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Shah of Naar Isle");
    }

    @Test
    @DisplayName("Paying echo lets each opponent draw up to three cards")
    void payingEchoLetsOpponentDrawThreeCards() {
        harness.setLibrary(player2, List.of(
                new ShahOfNaarIsle(), new ShahOfNaarIsle(), new ShahOfNaarIsle()));
        castShah();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent may draw zero cards after echo is paid")
    void opponentMayDrawZeroCards() {
        harness.setLibrary(player2, List.of(new ShahOfNaarIsle()));
        castShah();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining echo sacrifices Shah of Naar Isle")
    void decliningEchoSacrificesShah() {
        castShah();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Shah of Naar Isle");
        harness.assertInGraveyard(player1, "Shah of Naar Isle");
    }

    private void castShah() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new ShahOfNaarIsle(), "{3}{R}");

        resolveAllTriggers();
    }
}
