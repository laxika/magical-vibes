package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TalarasBattalionTest extends BaseCardTest {

    @Test
    @DisplayName("Castable after another green spell was cast this turn")
    void castableAfterGreenSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new TalarasBattalion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0); // Grizzly Bears (green)
        harness.passBothPriorities(); // resolve it, stack empties

        harness.castCreature(player1, 0); // Talara's Battalion

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Talara's Battalion");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Talara's Battalion"));
    }

    @Test
    @DisplayName("Not castable when no other spell was cast this turn")
    void notCastableWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new TalarasBattalion()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Not castable when only a non-green spell was cast this turn")
    void notCastableAfterNonGreenSpell() {
        harness.setHand(player1, List.of(new SuntailHawk(), new TalarasBattalion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0); // Suntail Hawk (white)
        harness.passBothPriorities(); // resolve it, stack empties

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
