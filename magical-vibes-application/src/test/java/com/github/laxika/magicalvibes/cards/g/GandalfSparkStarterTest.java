package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfSparkStarter.class, GrizzlyBears.class})
class GandalfSparkStarterTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToOneTarget() {
        harness.setLife(player2, 20);
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

        castGandalf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void dividesThreeDamageAmongTwoTargets() {
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 1, player2.getId(), 2);

        castGandalf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dividesThreeDamageAmongThreeTargets() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent firstBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(
                firstBears.getId(), 1,
                secondBears.getId(), 1,
                player2.getId(), 1
        );

        castGandalf();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstBears.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void castGandalf() {
        harness.setHand(player1, List.of(new GandalfSparkStarter()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
    }
}
