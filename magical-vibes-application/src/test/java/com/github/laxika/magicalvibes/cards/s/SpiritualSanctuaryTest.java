package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SpiritualSanctuary.class, Plains.class})
class SpiritualSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("The active player gains 1 life when they control a Plains")
    void activePlayerGainsLifeWithPlains() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The ability does nothing when the active player controls no Plains")
    void doesNotGainLifeWithoutPlains() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("On the opponent's upkeep, the opponent gains the life")
    void opponentGainsLifeOnTheirUpkeep() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player2, new Plains());
        harness.setLife(player2, 19);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
