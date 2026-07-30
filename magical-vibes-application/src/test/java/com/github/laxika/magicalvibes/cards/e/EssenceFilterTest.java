package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EssenceFilterTest extends BaseCardTest {

    private void castEssenceFilter(int mode) {
        harness.setHand(player1, List.of(new EssenceFilter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Mode 0 destroys every enchantment regardless of color")
    void allEnchantmentsMode() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new RootMaze());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEssenceFilter(0);

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Root Maze");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 destroys only nonwhite enchantments")
    void nonwhiteEnchantmentsMode() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new RootMaze());

        castEssenceFilter(1);

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Root Maze");
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new EssenceFilter()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Essence Filter goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castEssenceFilter(0);

        harness.assertInGraveyard(player1, "Essence Filter");
    }
}
