package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinelessThug.class, RootwaterCommando.class})
class SpinelessThugTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spineless Thug puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SpinelessThug()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Spineless Thug cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new SpinelessThug());

        Permanent attacker = addCreatureReady(player1, new RootwaterCommando());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
