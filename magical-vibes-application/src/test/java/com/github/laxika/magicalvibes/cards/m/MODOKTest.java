package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MODOK.class, GrizzlyBears.class, Swamp.class})
class MODOKTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures opponents control get -1/-1")
    void debuffsOpponentCreaturesOnly() {
        harness.addToBattlefield(player1, new MODOK());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying 3 life makes M.O.D.O.K. connive")
    void paysLifeAndConnives() {
        Permanent modok = addReadyMODOK();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(modok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the connive ability during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addReadyMODOK();
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a land while conniving does not add a counter")
    void landDiscardDoesNotAddCounter() {
        Permanent modok = addReadyMODOK();
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLibrary(player1, List.of(new MODOK()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(modok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertInHand(player1, "M.O.D.O.K.");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick M.O.D.O.K. can connive repeatedly during its controller's end step")
    void canActivateRepeatedlyDuringOwnEndStep() {
        Permanent modok = harness.addToBattlefieldAndReturn(player1, new MODOK());
        modok.setSummoningSick(true);
        modok.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MODOK(), new MODOK()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 17);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 14);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(modok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the connive cost with less than 3 life")
    void cannotActivateWithInsufficientLife() {
        addReadyMODOK();
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MODOK()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyMODOK() {
        return addCreatureReady(player1, new MODOK());
    }
}
