package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaggotCarrier.class, Unsummon.class})
class MaggotCarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes each player lose 1 life")
    void etbMakesEachPlayerLose1Life() {
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new MaggotCarrier(), "{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast under the opponent's control triggers life loss")
    void enteringWithoutBeingCastTriggersLifeLoss() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 13);

        harness.enterBattlefieldAndReturn(player2, new MaggotCarrier());

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 13);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB trigger still resolves after Maggot Carrier returns to hand")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 13);
        harness.castFromHand(player1, new MaggotCarrier(), "{B}");
        harness.passBothPriorities();

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 13);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Maggot Carrier"));

        harness.assertNotOnBattlefield(player1, "Maggot Carrier");
        harness.assertInHand(player1, "Maggot Carrier");
        harness.assertLife(player1, 8);
        harness.assertLife(player2, 13);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 12);
        assertThat(gd.stack).isEmpty();
    }
}
