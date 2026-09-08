package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SerratedScorpion.class)
class SerratedScorpionTest extends BaseCardTest {

    @Test
    void whenItDiesItDamagesEachOpponentAndGainsLife() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new SerratedScorpion());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, scorpion));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Serrated Scorpion");
    }
}
