package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MukotaiSoulripper.class, GrizzlyBears.class, Millstone.class})
class MukotaiSoulripperTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 2 animates Mukotai Soulripper and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addReadyVehicle(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another creature adds a counter and grants menace until end of turn")
    void attackingCanSacrificeCreatureForCounterAndMenace() {
        Permanent vehicle = crewVehicle();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger can sacrifice another artifact")
    void attackingCanSacrificeArtifact() {
        Permanent vehicle = crewVehicle();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Millstone());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the battlefield unchanged")
    void decliningSacrificeDoesNothing() {
        Permanent vehicle = crewVehicle();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    @DisplayName("Mukotai Soulripper cannot sacrifice itself for its attack trigger")
    void cannotSacrificeItself() {
        Permanent vehicle = crewVehicle();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.MENACE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vehicle);
    }

    private Permanent addReadyVehicle(com.github.laxika.magicalvibes.model.Player player) {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player, new MukotaiSoulripper());
        vehicle.setSummoningSick(false);
        return vehicle;
    }

    private Permanent crewVehicle() {
        Permanent vehicle = addReadyVehicle(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return vehicle;
    }
}
