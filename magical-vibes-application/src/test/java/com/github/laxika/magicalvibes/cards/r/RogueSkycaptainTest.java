package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
