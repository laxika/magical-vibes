package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraspingThrullTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to each opponent and gains 2 life")
    void etbDamagesEachOpponentAndGainsLife() {
        harness.setHand(player1, List.of(new GraspingThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }
}
