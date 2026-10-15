package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TunnelingGeopede.class, Forest.class})
class TunnelingGeopedeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent when your land enters")
    void landfallDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new TunnelingGeopede());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new TunnelingGeopede());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Landfall triggers for each land entering without being played")
    void landsEnteringWithoutBeingPlayedEachTrigger() {
        harness.addToBattlefield(player1, new TunnelingGeopede());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Geopede triggers independently for the same land")
    void multipleGeopedesEachDealDamage() {
        harness.addToBattlefield(player1, new TunnelingGeopede());
        harness.addToBattlefield(player1, new TunnelingGeopede());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Geopede damages you when their land enters")
    void opponentGeopedeDamagesItsOpponent() {
        harness.addToBattlefield(player2, new TunnelingGeopede());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
