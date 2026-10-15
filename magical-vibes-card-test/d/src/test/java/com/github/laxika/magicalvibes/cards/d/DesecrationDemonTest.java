package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesecrationDemon.class, DrudgeBeetle.class, UltimatePrice.class})
class DesecrationDemonTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT); // BEGINNING_OF_COMBAT — trigger fires
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Opponent declines — Demon stays untapped with no counter")
    void decliningLeavesDemonUntapped() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new DrudgeBeetle());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(demon.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Drudge Beetle");
    }

    @Test
    @DisplayName("Opponent accepts with one creature — it is sacrificed, Demon taps and grows")
    void acceptingSacrificesAndTapsDemon() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new DrudgeBeetle());

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player2, "Drudge Beetle");
        assertThat(demon.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(7);
    }

    @Test
    @DisplayName("Opponent with several creatures picks which one to sacrifice")
    void acceptingWithSeveralCreaturesAsksWhichOne() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());

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
        harness.addToBattlefield(player2, new DrudgeBeetle());

        advanceToCombatAndResolve(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Drudge Beetle");
        assertThat(demon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller is never offered the sacrifice")
    void controllerIsNotOffered() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DrudgeBeetle());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Drudge Beetle");
        assertThat(demon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing still adds a counter when the Demon is already tapped")
    void alreadyTappedDemonStillGrows() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        demon.tap();
        harness.addToBattlefield(player2, new DrudgeBeetle());

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Drudge Beetle");
        assertThat(demon.isTapped()).isTrue();
        assertThat(demon.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(7);
    }

    @Test
    @DisplayName("Sacrifices in successive combats add cumulative counters")
    void countersAccumulateAcrossCombats() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new DrudgeBeetle());
        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);

        harness.addToBattlefield(player2, new DrudgeBeetle());
        advanceToCombatAndResolve(player2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(demon.isTapped()).isTrue();
        assertThat(demon.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Opponent can sacrifice after the Demon leaves the battlefield in response")
    void triggerResolvesAfterSourceLeaves() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, demon.getId());
        harness.assertInGraveyard(player1, "Desecration Demon");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player2, "Drudge Beetle");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
