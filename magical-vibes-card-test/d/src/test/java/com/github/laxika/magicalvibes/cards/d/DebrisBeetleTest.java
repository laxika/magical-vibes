package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DebrisBeetle.class, DaringMechanic.class})
class DebrisBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("When Debris Beetle enters, each opponent loses 3 life and its controller gains 3 life")
    void etbDrainsEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DebrisBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Crew 2 animates Debris Beetle and taps the creature used to crew it")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent beetle = addCreatureReady(player1, new DebrisBeetle());
        Permanent crew = addCreatureReady(player1, new DaringMechanic());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, beetle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew, and animation waits for resolution")
    void freshCreatureCanCrew() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DebrisBeetle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new DaringMechanic());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, beetle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, beetle)).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A tapped creature cannot pay the crew cost")
    void tappedCreatureCannotCrew() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DebrisBeetle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new DaringMechanic());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, beetle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creatures cannot pay the crew cost")
    void opponentsCreatureCannotCrew() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DebrisBeetle());
        Permanent crew = harness.addToBattlefieldAndReturn(player2, new DaringMechanic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, beetle)).isFalse();
    }

    @Test
    @DisplayName("The enter trigger resolves even after Debris Beetle leaves the battlefield")
    void enterTriggerSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new DebrisBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        Permanent beetle = findPermanent(player1, "Debris Beetle");
        gd.playerBattlefields.get(player1.getId()).remove(beetle);
        gd.playerGraveyards.get(player1.getId()).add(beetle.getCard());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The enter trigger uses its controller, even when the opponent casts Beetle")
    void opponentsBeetleDrainsUs() {
        harness.enterBattlefieldAndReturn(player2, new DebrisBeetle());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Crew requires at least two total power")
    void insufficientPowerCannotCrew() {
        harness.addToBattlefield(player1, new DebrisBeetle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new DaringMechanic());
        crew.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew works with exactly two power and expires at end of turn")
    void exactPowerCrewExpires() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DebrisBeetle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new DaringMechanic());
        crew.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, beetle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, beetle)).isFalse();
    }

    @Test
    @DisplayName("A crewed Beetle tramples over a blocker")
    void crewedBeetleDealsTrampleDamage() {
        addCreatureReady(player1, new DebrisBeetle());
        addCreatureReady(player1, new DaringMechanic());
        Permanent blocker = addCreatureReady(player2, new DaringMechanic());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 3
        ));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Daring Mechanic");
        harness.assertOnBattlefield(player1, "Debris Beetle");
    }
}
