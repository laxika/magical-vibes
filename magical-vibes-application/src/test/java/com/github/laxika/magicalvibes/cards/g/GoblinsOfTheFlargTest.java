package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinsOfTheFlargTest extends BaseCardTest {

    @Test
    @DisplayName("Survives when its controller controls no Dwarves")
    void survivesWithoutDwarf() {
        castGoblins();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Is sacrificed when its controller controls a Dwarf")
    void sacrificedWithDwarf() {
        harness.addToBattlefield(player1, new DwarvenTrader());
        castGoblins();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblins of the Flarg");
        harness.assertInGraveyard(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Is sacrificed when a Dwarf later becomes controlled")
    void sacrificedWhenDwarfEnters() {
        castGoblins();
        harness.addToBattlefield(player1, new DwarvenTrader());
        harness.runStateBasedActions();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblins of the Flarg");
        harness.assertInGraveyard(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("An opponent's Dwarf does not cause the sacrifice")
    void opponentDwarfDoesNotCount() {
        harness.addToBattlefield(player2, new DwarvenTrader());
        castGoblins();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
    }

    private void castGoblins() {
        harness.setHand(player1, List.of(new GoblinsOfTheFlarg()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
