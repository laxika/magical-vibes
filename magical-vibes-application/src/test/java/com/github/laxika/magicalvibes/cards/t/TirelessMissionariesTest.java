package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessMissionaries.class})
class TirelessMissionariesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tireless Missionaries puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castTirelessMissionaries();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tireless Missionaries");
    }

    @Test
    @DisplayName("Resolving puts Tireless Missionaries on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castTirelessMissionaries();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Tireless Missionaries");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tireless Missionaries");
    }

    @Test
    @DisplayName("ETB trigger causes controller to gain 3 life")
    void etbGainsLife() {
        castTirelessMissionaries();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castTirelessMissionaries();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castTirelessMissionaries();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast gives life to its controller only after the trigger resolves")
    void enteringWithoutCastingGainsLifeForOtherController() {
        harness.enterBattlefieldAndReturn(player2, new TirelessMissionaries());

        harness.assertOnBattlefield(player2, "Tireless Missionaries");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }

    private void castTirelessMissionaries() {
        harness.castFromHand(player1, new TirelessMissionaries(), "{4}{W}");
    }
}
