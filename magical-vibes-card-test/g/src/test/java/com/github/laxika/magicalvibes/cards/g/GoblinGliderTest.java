package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinGlider.class, RagingGoblin.class})
class GoblinGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin Glider cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new GoblinGlider());

        addCreatureReady(player1, new RagingGoblin());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Goblin Glider")
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new GoblinGlider());
        addCreatureReady(player2, new RagingGoblin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Goblin Glider cannot block even an attacking flyer")
    void cannotBlockFlyingAttacker() {
        addCreatureReady(player1, new GoblinGlider());
        addCreatureReady(player2, new GoblinGlider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Goblin Glider can attack and deal unblocked combat damage")
    void dealsUnblockedCombatDamage() {
        addCreatureReady(player1, new GoblinGlider());
        addCreatureReady(player2, new RagingGoblin());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Goblin Glider");
    }
}
