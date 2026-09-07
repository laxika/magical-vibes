package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadGone.class, GrizzlyBears.class})
class DeadGoneTest extends BaseCardTest {

    @Test
    void deadDealsTwoDamageToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castModalInstant(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void goneReturnsTargetCreatureYouDoNotControlToItsOwnersHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstant(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void goneCannotTargetACreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DeadGone()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
