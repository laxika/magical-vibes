package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkoumHellkite.class, Forest.class, Mountain.class})
class AkoumHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when a Mountain enters under your control")
    void mountainLandfallDealsTwoDamage() {
        harness.addToBattlefield(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 1 damage when a non-Mountain land enters under your control")
    void nonMountainLandfallDealsOneDamage() {
        harness.addToBattlefield(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
