package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainOfEmbers.class, BorosRecruit.class, Watchwolf.class})
class RainOfEmbersTest extends BaseCardTest {

    @Test
    @DisplayName("Rain of Embers deals 1 damage to each creature and each player")
    void dealsDamageToAllCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BorosRecruit());
        harness.addToBattlefield(player2, new Watchwolf());

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.assertOnBattlefield(player2, "Watchwolf");
        assertThat(findPermanent(player2, "Watchwolf").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
