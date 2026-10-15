package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraspingThrull.class, Mortify.class})
class GraspingThrullTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to each opponent and gains 2 life")
    void etbDamagesEachOpponentAndGainsLife() {
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new GraspingThrull(), "{3}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves after Grasping Thrull is destroyed")
    void etbResolvesAfterSourceLeavesBattlefield() {
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new GraspingThrull(), "{3}{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);

        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Grasping Thrull"));

        harness.assertInGraveyard(player1, "Grasping Thrull");
        harness.assertNotOnBattlefield(player1, "Grasping Thrull");
        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore + 2);
        harness.assertLife(player2, opponentLifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }
}
