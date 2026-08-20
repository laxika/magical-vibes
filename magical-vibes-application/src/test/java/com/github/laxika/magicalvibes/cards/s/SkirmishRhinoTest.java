package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkirmishRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent lose 2 life and its controller gain 2 life")
    void entersBattlefieldDrainsOpponentsAndGainsLife() {
        castSkirmishRhino();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB trigger is put on the stack after the creature resolves")
    void entersBattlefieldPutsTriggerOnStack() {
        castSkirmishRhino();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    private void castSkirmishRhino() {
        harness.setHand(player1, List.of(new SkirmishRhino()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }
}
