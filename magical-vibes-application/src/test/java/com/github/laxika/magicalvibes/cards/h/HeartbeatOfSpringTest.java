package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShizoDeathsStorehouse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartbeatOfSpring.class, Forest.class, ShizoDeathsStorehouse.class})
class HeartbeatOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a land for mana adds one additional mana of the type it produced")
    void addsExtraManaForController() {
        harness.addToBattlefield(player1, new HeartbeatOfSpring());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's land also produces the additional mana")
    void addsExtraManaForOpponent() {
        harness.addToBattlefield(player1, new HeartbeatOfSpring());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonbasic land's mana ability also gets the additional mana")
    void addsExtraManaForActivatedLandAbility() {
        harness.addToBattlefield(player1, new HeartbeatOfSpring());
        harness.addToBattlefield(player1, new ShizoDeathsStorehouse());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }
}
