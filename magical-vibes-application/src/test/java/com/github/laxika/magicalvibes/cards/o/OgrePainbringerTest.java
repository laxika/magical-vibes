package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OgrePainbringerTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToEachPlayer() {
        OgrePainbringer painbringer = new OgrePainbringer();
        // This tutorial-only card has no provider-backed printing; exercise its effect directly.
        painbringer.setName("Ogre Painbringer");
        harness.enterBattlefieldAndReturn(player1, painbringer);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
