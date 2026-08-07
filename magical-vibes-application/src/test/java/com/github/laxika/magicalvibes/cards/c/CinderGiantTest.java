package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CinderGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 2 damage to each other creature you control only")
    void upkeepTriggerDamagesOwnOtherCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CinderGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());   // 2/2 dies
        harness.addToBattlefield(player2, new GrizzlyBears());   // opponent's, untouched
        harness.addToBattlefield(player2, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
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
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
