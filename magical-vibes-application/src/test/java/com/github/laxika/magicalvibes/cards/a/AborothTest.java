package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Aboroth.class)
class AborothTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep puts a -1/-1 counter on Aboroth and keeps it")
    void paysCumulativeUpkeep() {
        Permanent aboroth = harness.addToBattlefieldAndReturn(player1, new Aboroth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(aboroth.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aboroth);
        assertThat(aboroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Aboroth")
    void declineSacrifices() {
        Permanent aboroth = harness.addToBattlefieldAndReturn(player1, new Aboroth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aboroth);
        harness.assertInGraveyard(player1, "Aboroth");
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Aboroth's controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent aboroth = harness.addToBattlefieldAndReturn(player1, new Aboroth());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(aboroth.getCounterCount(CounterType.AGE)).isZero();
        harness.assertOnBattlefield(player1, "Aboroth");
    }

    @Test
    @DisplayName("Second upkeep puts two more -1/-1 counters on Aboroth")
    void secondUpkeepPutsTwoCounters() {
        Permanent aboroth = harness.addToBattlefieldAndReturn(player1, new Aboroth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(aboroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(aboroth.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        // One counter per age counter: 1 + 2 = 3, leaving a 9/9 as a 6/6.
        assertThat(aboroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aboroth);
    }

    @Test
    @CardUsed(MeliraSylvokOutcast.class)
    @DisplayName("Cumulative upkeep cannot be paid when -1/-1 counters are prohibited")
    void cannotPayWhenMinusCountersAreProhibited() {
        Permanent aboroth = harness.addToBattlefieldAndReturn(player1, new Aboroth());
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(aboroth.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertNotOnBattlefield(player1, "Aboroth");
        harness.assertInGraveyard(player1, "Aboroth");
        assertThat(aboroth.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
