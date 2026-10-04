package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighwayRobber.class, Shock.class})
class HighwayRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Highway Robber puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

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

    @Test
    @DisplayName("ETB trigger causes target opponent to lose 2 life and controller to gain 2 life")
    void etbDrainsLife() {
        castHighwayRobber();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castHighwayRobber();
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castHighwayRobber();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records both life loss and life gain")
    void gameLogRecordsLifeChanges() {
        castHighwayRobber();
        resolveAllTriggers();

        assertThat(gameLogContains("loses 2 life")).isTrue();
        assertThat(gameLogContains("gains 2 life")).isTrue();
    }

    @Test
    @DisplayName("Life totals do not change until the ETB trigger resolves")
    void lifeDrainWaitsForTriggerResolution() {
        castHighwayRobber();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing Highway Robber does not stop its pending life drain")
    void lifeDrainResolvesAfterSourceDies() {
        castHighwayRobber();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Highway Robber"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Highway Robber");
        harness.assertNotOnBattlefield(player1, "Highway Robber");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castHighwayRobber() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, player2.getId());
    }
}

