package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RyuseiTheFallingStarTest extends BaseCardTest {

    @Test
    @DisplayName("When Ryusei dies, it deals 5 damage to each creature without flying")
    void deathTriggerBurnsNonFlyers() {
        Permanent ryusei = harness.addToBattlefieldAndReturn(player1, new RyuseiTheFallingStar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());

        ryusei.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.runStateBasedActions();

        // Both non-flyers took 5 and died; the flyer is untouched.
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Hill Giant")).isEmpty();
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isZero();
    }
}
