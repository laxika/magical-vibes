package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorfootGriffin.class, RagingKavu.class})
class RazorfootGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Razorfoot Griffin")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new RazorfootGriffin());
        addCreatureReady(player2, new RagingKavu());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A first-strike blocker kills a 3/1 attacker before it deals regular damage")
    void firstStrikeKillsAttackerBeforeRegularDamage() {
        addCreatureReady(player1, new RagingKavu());
        addCreatureReady(player2, new RazorfootGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertInGraveyard(player1, "Raging Kavu");
    }
}
