package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Plains.class)
class PlainsTest extends BaseCardTest {

    @Test
    void tapsForOneWhiteMana() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.tapPermanent(player1, 0);

        assertThat(plains.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
