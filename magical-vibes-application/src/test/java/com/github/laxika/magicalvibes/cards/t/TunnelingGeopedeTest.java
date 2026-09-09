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
}
