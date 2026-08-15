package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathforgeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it deals no damage")
    void dealsNoDamageWithoutMultikicker() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("It deals twice the number of multikicker payments")
    void dealsTwiceTheNumberOfMultikickerPayments() {
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castWithMultikickerPayments(List.of("{R}", "{R}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The ETB trigger cannot target a creature")
    void cannotTargetCreature() {
        var creature = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.addToBattlefield(player2, creature);
        harness.setHand(player1, List.of(new DeathforgeShaman()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    private void castWithMultikickerPayments(List<String> payments) {
        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
    }
}
