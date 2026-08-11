package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SummitSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("When Summit Sentinel dies, its controller draws a card")
    void diesDrawsCard() {
        harness.addToBattlefield(player1, new SummitSentinel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Summit Sentinel");
        harness.assertInGraveyard(player1, "Summit Sentinel");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }
}
