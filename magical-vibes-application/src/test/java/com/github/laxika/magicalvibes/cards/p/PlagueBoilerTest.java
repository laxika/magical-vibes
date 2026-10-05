package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VinelasherKudzu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueBoiler.class, Forest.class, VinelasherKudzu.class})
class PlagueBoilerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a plague counter at upkeep and destroys nonland permanents at three counters")
    void upkeepCounterTriggersBoardWipeAtThreeCounters() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        boiler.setCounterCount(CounterType.PLAGUE, 2);
        harness.addToBattlefield(player1, new VinelasherKudzu());
        harness.addToBattlefield(player2, new VinelasherKudzu());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Boiler");
        harness.assertInGraveyard(player1, "Vinelasher Kudzu");
        harness.assertInGraveyard(player2, "Vinelasher Kudzu");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Does not destroy permanents if it leaves before its state trigger resolves")
    void stateTriggerDoesNothingWhenBoilerLeavesBeforeResolution() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        boiler.setCounterCount(CounterType.PLAGUE, 2);
        harness.addToBattlefield(player1, new VinelasherKudzu());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(3);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, boiler));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Boiler");
        harness.assertOnBattlefield(player1, "Vinelasher Kudzu");
    }

    @Test
    @DisplayName("The activated ability adds or removes a plague counter")
    void activatedAbilityAddsOrRemovesCounter() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a plague counter on Plague Boiler");
        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);

        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Remove a plague counter from Plague Boiler");
        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isZero();
    }

    @Test
    @DisplayName("Cannot choose to remove a plague counter when there are none")
    void cannotChooseRemovalWithoutPlagueCounters() {
        harness.addToBattlefield(player1, new PlagueBoiler());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1,
                "Remove a plague counter from Plague Boiler"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Removing a counter in response does not stop the sacrifice and destruction")
    void removingCounterDoesNotStopPendingStateTrigger() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        boiler.setCounterCount(CounterType.PLAGUE, 2);
        harness.addToBattlefield(player2, new VinelasherKudzu());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Plague Boiler");

        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Remove a plague counter from Plague Boiler");
        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Boiler");
        harness.assertInGraveyard(player2, "Vinelasher Kudzu");
    }

    @Test
    @DisplayName("Adding the third counter with the activated ability triggers destruction")
    void activatedThirdCounterTriggersDestruction() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        boiler.setCounterCount(CounterType.PLAGUE, 2);
        harness.addToBattlefield(player2, new VinelasherKudzu());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a plague counter on Plague Boiler");

        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Plague Boiler");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Boiler");
        harness.assertInGraveyard(player2, "Vinelasher Kudzu");
    }

    @Test
    @DisplayName("The state trigger also fires with more than three counters")
    void moreThanThreeCountersTriggersDestruction() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        boiler.setCounterCount(CounterType.PLAGUE, 4);
        harness.addToBattlefield(player2, new VinelasherKudzu());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Boiler");
        harness.assertInGraveyard(player2, "Vinelasher Kudzu");
    }

    @Test
    @DisplayName("An opponent's upkeep does not add a plague counter")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());

        advanceToUpkeep(player2);

        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
