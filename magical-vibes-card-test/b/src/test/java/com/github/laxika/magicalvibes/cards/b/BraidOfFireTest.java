package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RonomUnicorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidOfFire.class, RonomUnicorn.class})
class BraidOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep adds one red mana")
    void addsOneRedMana() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(braid.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Braid of Fire");
    }

    @Test
    @DisplayName("Braid of Fire adds its mana to the controller's existing red mana")
    void addsToExistingRedMana() {
        harness.addToBattlefield(player1, new BraidOfFire());
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Braid of Fire adds one red mana per age counter")
    void addsRedManaForEachAgeCounter() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());
        braid.setCounterCount(CounterType.AGE, 1);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(braid.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining Braid of Fire's cumulative upkeep sacrifices it")
    void decliningCumulativeUpkeepSacrifices() {
        harness.addToBattlefield(player1, new BraidOfFire());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Braid of Fire");
        harness.assertInGraveyard(player1, "Braid of Fire");
    }

    @Test
    @DisplayName("The upkeep trigger uses the stack and adds no mana before payment")
    void doesNotAddManaBeforePayment() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            assertThat(gd.stack).hasSize(1);
            assertThat(braid.getCounterCount(CounterType.AGE)).isZero();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

            harness.passBothPriorities();
            assertThat(braid.getCounterCount(CounterType.AGE)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Mana is added only during its controller's upkeep and to that controller")
    void onlyTriggersDuringControllersUpkeep() {
        Permanent braid = harness.addToBattlefieldAndReturn(player2, new BraidOfFire());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            assertThat(gd.stack).isEmpty();
            assertThat(braid.getCounterCount(CounterType.AGE)).isZero();

            advanceToUpkeep(player2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);

            assertThat(braid.getCounterCount(CounterType.AGE)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        });
    }

    @Test
    @DisplayName("Unused mana empties before the draw step without losing life")
    void unusedManaEmptiesBeforeDrawStep() {
        harness.addToBattlefield(player1, new BraidOfFire());
        int startingLife = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        });
        harness.passUntil(player1, TurnStep.DRAW);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Removing Braid of Fire before its upkeep resolves produces no mana or payment choice")
    void removedSourceDoesNotProduceMana() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());
        harness.addToBattlefield(player1, new RonomUnicorn());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.activateAbility(player1, 1, null, braid.getId());
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Braid of Fire");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        });
    }
}
