package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.g.GuidelightPathmaker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessVelocitaur.class, GuidelightPathmaker.class, BrightfieldGlider.class})
class RecklessVelocitaurTest extends BaseCardTest {

    @Test
    void crewsVehicleDuringMainPhaseAndBoostsItWithTrampleUntilEndOfTurn() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNotTriggerOutsideMainPhase() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addVehicleReady(Player player) {
        return addCreatureReady(player, new GuidelightPathmaker());
    }

    @Test
    void saddlesMountDuringMainPhaseAndBoostsOnlyTheMount() {
        Permanent pilot = addCreatureReady(player1, new RecklessVelocitaur());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        int pilotPower = gqs.getEffectivePower(gd, pilot);
        int mountPower = gqs.getEffectivePower(gd, mount);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, mount)).isEqualTo(mountPower + 2);
        assertThat(gqs.hasKeyword(gd, mount, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(pilotPower);
        assertThat(gqs.hasKeyword(gd, pilot, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void triggersDuringPostcombatMainPhase() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsMainPhase() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void powerBoostAndTrampleResolveAsOneAbilityBeforeCrew() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isTrue();
        resolveAllTriggers();
    }
}
