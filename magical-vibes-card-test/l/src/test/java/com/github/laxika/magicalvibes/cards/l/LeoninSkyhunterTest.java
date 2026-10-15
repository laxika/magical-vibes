package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninSkyhunter.class, GrizzlyBears.class, GiantSpider.class})
class LeoninSkyhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Leonin Skyhunter")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Leonin Skyhunter")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new LeoninSkyhunter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Leonin Skyhunter");
        harness.assertInGraveyard(player2, "Leonin Skyhunter");
    }

    @Test
    @DisplayName("A creature with reach can block Leonin Skyhunter")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Leonin Skyhunter");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Leonin Skyhunter can block a creature without flying")
    void canBlockGroundCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new LeoninSkyhunter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Leonin Skyhunter");
    }
}
