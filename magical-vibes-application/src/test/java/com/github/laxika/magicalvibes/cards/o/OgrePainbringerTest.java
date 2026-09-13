package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OgrePainbringer.class)
class OgrePainbringerTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToEachPlayer() {
        OgrePainbringer painbringer = new OgrePainbringer();
        if (painbringer.getName() == null) {
            painbringer.setName("Ogre Painbringer");
        }
        harness.enterBattlefieldAndReturn(player1, painbringer);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
