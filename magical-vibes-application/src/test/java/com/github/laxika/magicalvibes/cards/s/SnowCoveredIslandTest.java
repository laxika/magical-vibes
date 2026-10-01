package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnowCoveredIsland.class})
class SnowCoveredIslandTest extends BaseCardTest {

    @Test
    void tapsForOneBlueMana() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());

        harness.tapPermanent(player1, 0);

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
