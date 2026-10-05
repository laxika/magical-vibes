package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiggyBank.class, WrathOfGod.class})
class PiggyBankTest extends BaseCardTest {

    @Test
    @DisplayName("When Piggy Bank dies, it creates a Treasure token")
    void createsTreasureWhenItDies() {
        harness.addToBattlefield(player1, new PiggyBank());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Each Piggy Bank creates its own Treasure when they die together")
    void simultaneousDeathsEachCreateTreasure() {
        harness.addToBattlefield(player1, new PiggyBank());
        harness.addToBattlefield(player1, new PiggyBank());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Piggy Bank");
    }

    @Test
    @DisplayName("An opponent's dying Piggy Bank creates Treasure for that opponent")
    void opponentReceivesTheirTreasure() {
        harness.addToBattlefield(player2, new PiggyBank());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure").getFirst().isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Piggy Bank");
    }
}
