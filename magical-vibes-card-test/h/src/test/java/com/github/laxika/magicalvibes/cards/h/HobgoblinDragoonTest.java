package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HobgoblinDragoon.class, NipGwyllion.class})
class HobgoblinDragoonTest extends BaseCardTest {

    @Test
    void creatureWithoutFlyingOrReachCannotBlock() {
        addCreatureReady(player1, new HobgoblinDragoon());
        addCreatureReady(player2, new NipGwyllion());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockAndNeitherFirstStrikerDealsDamageTwice() {
        addCreatureReady(player1, new HobgoblinDragoon());
        addCreatureReady(player2, new HobgoblinDragoon());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Hobgoblin Dragoon");
        harness.assertOnBattlefield(player2, "Hobgoblin Dragoon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeKillsGroundAttackerBeforeItCanDealDamageOrGainLife() {
        addCreatureReady(player1, new NipGwyllion());
        addCreatureReady(player2, new HobgoblinDragoon());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Nip Gwyllion");
        harness.assertNotOnBattlefield(player1, "Nip Gwyllion");
        harness.assertOnBattlefield(player2, "Hobgoblin Dragoon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new HobgoblinDragoon());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Hobgoblin Dragoon");
    }
}
