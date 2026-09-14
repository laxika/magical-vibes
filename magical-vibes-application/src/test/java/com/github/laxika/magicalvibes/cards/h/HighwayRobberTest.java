package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighwayRobber.class})
class HighwayRobberTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Highway Robber puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Resolving creature spell =====

    @Test
    @DisplayName("Resolving puts Highway Robber on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Highway Robber");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target its controller with the ETB ability")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== ETB life drain =====

    @Test
    @DisplayName("ETB trigger causes target opponent to lose 2 life and controller to gain 2 life")
    void etbDrainsLife() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records both life loss and life gain")
    void gameLogRecordsLifeChanges() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gameLogContains("loses 2 life")).isTrue();
        assertThat(gameLogContains("gains 2 life")).isTrue();
    }

    // ===== Helpers =====

    private void castHighwayRobber() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, player2.getId());
    }
}

