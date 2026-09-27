package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoyousRespite.class, Forest.class, HumbleBudoka.class})
class JoyousRespiteTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each land the controller controls")
    void gainsLifePerLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new HumbleBudoka());
        harness.addToBattlefield(player2, new Forest());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new JoyousRespite(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains no life when the controller controls no lands")
    void gainsNoLifeWithoutLands() {
        harness.addToBattlefield(player2, new Forest());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new JoyousRespite(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts lands when the spell resolves")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new JoyousRespite(), "{3}{G}");
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
