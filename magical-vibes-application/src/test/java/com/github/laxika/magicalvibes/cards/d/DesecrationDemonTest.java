package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DesecrationDemonTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // BEGINNING_OF_COMBAT — trigger fires
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Opponent declines — Demon stays untapped with no counter")
    void decliningLeavesDemonUntapped() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(demon.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent accepts with one creature — it is sacrificed, Demon taps and grows")
    void acceptingSacrificesAndTapsDemon() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(demon.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(7);
    }

    @Test
    @DisplayName("Opponent with several creatures picks which one to sacrifice")
    void acceptingWithSeveralCreaturesAsksWhichOne() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(demon.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(7);
    }

    @Test
    @DisplayName("Opponent with no creatures is not prompted and the Demon is unaffected")
    void noCreaturesMeansNoPrompt() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(demon.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(6);
    }

    @Test
    @DisplayName("Triggers during the opponent's combat too")
    void triggersOnOpponentsCombat() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(demon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller is never offered the sacrifice")
    void controllerIsNotOffered() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(demon.isTapped()).isFalse();
    }
}
