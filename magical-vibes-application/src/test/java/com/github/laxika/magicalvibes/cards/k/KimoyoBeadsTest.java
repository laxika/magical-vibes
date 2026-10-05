package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KimoyoBeads.class})
class KimoyoBeadsTest extends BaseCardTest {

    private static final String AV_BEAD = "AV Bead \u2014 Draw a card.";
    private static final String COMMUNICATION_BEAD =
            "Communication Bead \u2014 Create two 1/1 white Soldier creature tokens.";
    private static final String PRIME_BEAD =
            "Prime Bead \u2014 You gain 3 life. Exile this artifact, then return it to the battlefield under its owner's control.";

    @Test
    @DisplayName("AV Bead draws a card")
    void avBeadDrawsCard() {
        harness.addToBattlefield(player1, new KimoyoBeads());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep();
        harness.handleListChoice(player1, AV_BEAD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Communication Bead creates two white Soldier tokens")
    void communicationBeadCreatesSoldiers() {
        harness.addToBattlefield(player1, new KimoyoBeads());

        advanceToEndStep();
        harness.handleListChoice(player1, COMMUNICATION_BEAD);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier"))
                .allMatch(soldier -> soldier.getCard().getColor() == CardColor.WHITE);
    }

    @Test
    @DisplayName("Prime Bead gains life and returns the artifact under its owner's control")
    void primeBeadGainsLifeAndFlickers() {
        Permanent beads = harness.addToBattlefieldAndReturn(player1, new KimoyoBeads());
        harness.setLife(player1, 20);

        advanceToEndStep();
        harness.handleListChoice(player1, PRIME_BEAD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Kimoyo Beads");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(returned.getId()).isNotEqualTo(beads.getId());
    }

    @Test
    @DisplayName("A mode cannot be chosen twice while the same artifact remains on the battlefield")
    void chosenModeIsConsumed() {
        harness.addToBattlefield(player1, new KimoyoBeads());
        harness.setLibrary(player1, List.of(new KimoyoBeads(), new KimoyoBeads()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToEndStep();
        harness.handleListChoice(player1, AV_BEAD);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(
                com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class);

        assertThatThrownBy(() -> harness.handleListChoice(player1, AV_BEAD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returning as a new object makes previously chosen modes available again")
    void primeBeadResetsPreviouslyChosenModes() {
        harness.addToBattlefield(player1, new KimoyoBeads());
        harness.setLibrary(player1, List.of(new KimoyoBeads(), new KimoyoBeads()));
        harness.setLife(player1, 20);

        advanceToEndStep();
        harness.handleListChoice(player1, AV_BEAD);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.handleListChoice(player1, PRIME_BEAD);
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();
        harness.handleListChoice(player1, AV_BEAD);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);

        advanceToEndStep();
        harness.handleListChoice(player1, PRIME_BEAD);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
        assertThat(findPermanents(player1, "Kimoyo Beads")).hasSize(1);
    }

    @Test
    @DisplayName("Prime Bead benefits the controller but returns the artifact to its owner")
    void primeBeadReturnsToDifferentOwner() {
        KimoyoBeads card = new KimoyoBeads();
        card.setOwnerId(player2.getId());
        Permanent beads = harness.addToBattlefieldAndReturn(player1, card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep();
        harness.handleListChoice(player1, PRIME_BEAD);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Kimoyo Beads");
        Permanent returned = findPermanent(player2, "Kimoyo Beads");
        assertThat(returned.getId()).isNotEqualTo(beads.getId());
    }

    @Test
    @DisplayName("Kimoyo Beads does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new KimoyoBeads());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
