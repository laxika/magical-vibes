package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhoenixFleetAirship.class, BottleGnomes.class})
class PhoenixFleetAirshipTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a token when no permanent was sacrificed")
    void doesNotCreateTokenWithoutSacrifice() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a token copy at your end step after you sacrifice a permanent")
    void createsTokenCopyAfterSacrifice() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int gnomesIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gnomes);
        harness.activateAbility(player1, gnomesIndex, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(2);
        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Becomes an artifact creature with eight permanents of its name")
    void becomesCreatureAtEightNamedPermanents() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new PhoenixFleetAirship());
        }

        Permanent airship = harness.addToBattlefieldAndReturn(player1, new PhoenixFleetAirship());

        assertThat(gqs.isCreature(gd, airship)).isTrue();
    }

    @Test
    @DisplayName("Opponent's named permanents do not count toward the creature threshold")
    void opponentNamedPermanentsDoNotCount() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new PhoenixFleetAirship());
        }
        Permanent airship = harness.addToBattlefieldAndReturn(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player2, new PhoenixFleetAirship());

        assertThat(gqs.isCreature(gd, airship)).isFalse();
    }

    @Test
    @DisplayName("Crew 1 taps a creature and animates the Airship below its threshold")
    void crewAnimatesBelowThreshold() {
        Permanent airship = harness.addToBattlefieldAndReturn(player1, new PhoenixFleetAirship());
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.isCreature(gd, airship)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gnomes.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, airship)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, airship)).isFalse();
    }

    @Test
    @DisplayName("An opponent's sacrifice does not enable your end-step trigger")
    void opponentSacrificeDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing after the end step begins does not create a copy")
    void sacrificeAfterEndStepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player1, new BottleGnomes());
        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(1);
    }

    @Test
    @DisplayName("Multiple sacrifices still create just one copy per Airship")
    void multipleSacrificesCreateOneCopy() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(2);
    }

    @Test
    @DisplayName("A seventh Airship's copy reaches the threshold without triggering again that end step")
    void tokenCopyReachesThreshold() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new PhoenixFleetAirship());
        }
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 7, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(8)
                .allMatch(permanent -> gqs.isCreature(gd, permanent));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(14);
    }

    @Test
    @DisplayName("A token copy retains the sacrifice-triggered ability on a later turn")
    void tokenCopyRetainsTriggeredAbility() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        advanceToEndStep();
        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();
        advanceToEndStep();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(4);
    }

    @Test
    @DisplayName("Your sacrifice does not create a copy at the opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(1);
    }

    @Test
    @DisplayName("A sacrifice on a previous turn does not enable a later end-step trigger")
    void previousTurnSacrificeDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new PhoenixFleetAirship());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        advanceToEndStep();
        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        advanceToEndStep();

        assertThat(findPermanents(player1, "Phoenix Fleet Airship")).hasSize(2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
