package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RogueSkycaptain.class, Solemnity.class})
class RogueSkycaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep adds a wage counter and paying the wages keeps the captain")
    void payingWagesKeepsCaptain() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying multiple wages charges two mana for each wage counter")
    void payingMultipleWagesChargesPerCounter() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        captain.setCounterCount(CounterType.WAGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Unable to pay all wages leaves the mana pool untouched and transfers the captain")
    void unableToPayAllWagesTransfersCaptainWithoutPartialPayment() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        captain.setCounterCount(CounterType.WAGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(captain);
    }

    @Test
    @DisplayName("Not paying removes all wage counters and hands the captain to the opponent")
    void decliningWagesGivesCaptainToOpponent() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        captain.setCounterCount(CounterType.WAGE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Third counter goes on before the payment is sized, so the cost is {6}.
        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(captain);
    }

    @Test
    @DisplayName("The opponent's upkeep does not add wage counters")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
    }

    @Test
    @DisplayName("Declining wages preserves counters of other types")
    void decliningWagesOnlyRemovesWageCounters() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(captain);
    }

    @Test
    @DisplayName("The new controller pays fresh wages and can later return the captain")
    void newControllerPaysWagesAndCanDeclineLater() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(captain);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(captain.getCounterCount(CounterType.WAGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(captain);
    }

    @Test
    @DisplayName("With no wage counters the controller may decline the zero-mana payment")
    void canDeclineZeroWagesWhenCountersArePrevented() {
        harness.addToBattlefield(player1, new Solemnity());
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(captain);
    }

    @Test
    @DisplayName("With no wage counters the controller may acknowledge payment without mana")
    void canPayZeroWagesWhenCountersArePrevented() {
        harness.addToBattlefield(player1, new Solemnity());
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new RogueSkycaptain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.WAGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(captain);
    }
}
