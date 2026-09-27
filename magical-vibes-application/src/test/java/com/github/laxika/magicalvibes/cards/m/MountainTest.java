package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mountain.class})
class MountainTest extends BaseCardTest {

    @Test
    void tapsForOneRedMana() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.tapPermanent(player1, 0);

        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
