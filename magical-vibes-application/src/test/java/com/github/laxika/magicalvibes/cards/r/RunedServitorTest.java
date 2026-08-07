package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunedServitorTest extends BaseCardTest {

    @Test
    @DisplayName("When Runed Servitor dies, each player draws a card")
    void diesThenEachPlayerDraws() {
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        Permanent servitor = harness.addToBattlefieldAndReturn(player1, new RunedServitor());
        servitor.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Runed Servitor");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1 + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 1);
    }
}
