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

    @Test
    @DisplayName("Rain of Embers damages both players with no creatures on the battlefield")
    void damagesPlayersOnEmptyBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Rain of Embers");
    }

    @Test
    @DisplayName("Rain of Embers damages every creature on both sides regardless of controller")
    void damagesMultipleCreaturesOnBothBattlefields() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BorosRecruit());
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.addToBattlefield(player2, new Watchwolf());

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertInGraveyard(player2, "Boros Recruit");
        harness.assertOnBattlefield(player1, "Watchwolf");
        harness.assertOnBattlefield(player2, "Watchwolf");
        assertThat(findPermanent(player1, "Watchwolf").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Watchwolf").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
