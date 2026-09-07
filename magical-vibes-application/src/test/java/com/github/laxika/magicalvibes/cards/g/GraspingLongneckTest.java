package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GraspingLongneck.class)
class GraspingLongneckTest extends BaseCardTest {

    @Test
    void gainsTwoLifeWhenItDies() {
        harness.setLife(player1, 10);
        Permanent longneck = harness.addToBattlefieldAndReturn(player1, new GraspingLongneck());
        longneck.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }
}
