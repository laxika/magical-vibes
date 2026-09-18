package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(RavenousGiant.class)
class RavenousGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller during their upkeep")
    void dealsDamageDuringControllerUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new RavenousGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new RavenousGiant());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }
}
