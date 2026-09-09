package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.d.DuskriderFalcon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderGiant.class, BenalishInfantry.class, DuskriderFalcon.class})
class CinderGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 2 damage to each other creature you control only")
    void upkeepTriggerDamagesOwnOtherCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CinderGiant());
        harness.addToBattlefield(player1, new DuskriderFalcon());   // 1/1 dies
        harness.addToBattlefield(player2, new DuskriderFalcon());   // opponent's, untouched
        harness.addToBattlefield(player2, new DuskriderFalcon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Duskrider Falcon");
        assertThat(countPermanents(player2, "Duskrider Falcon")).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep trigger deals exactly 2 damage to each other creature you control")
    void dealsTwoDamageToEachOtherCreatureYouControl() {
        harness.addToBattlefield(player1, new CinderGiant());
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player2, new BenalishInfantry());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Benalish Infantry").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Benalish Infantry").get(1).getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Benalish Infantry").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cinder Giant does not damage itself")
    void doesNotDamageItself() {
        harness.addToBattlefield(player1, new CinderGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cinder Giant");
        assertThat(findPermanent(player1, "Cinder Giant").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does not fire on the opponent's upkeep")
    void doesNotFireOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new CinderGiant());
        harness.addToBattlefield(player1, new DuskriderFalcon());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Duskrider Falcon");
    }
}
