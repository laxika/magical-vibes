package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HewedStoneRetainersTest extends BaseCardTest {

    @Test
    @DisplayName("Castable after another spell was cast this turn")
    void castableAfterAnotherSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new HewedStoneRetainers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0); // Grizzly Bears
        harness.passBothPriorities();

        harness.castCreature(player1, 0); // Hewed Stone Retainers

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hewed Stone Retainers");
    }

    @Test
    @DisplayName("Not castable when no other spell was cast this turn")
    void notCastableWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new HewedStoneRetainers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
