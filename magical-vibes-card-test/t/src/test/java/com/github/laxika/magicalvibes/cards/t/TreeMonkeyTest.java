package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreeMonkey.class, AirElemental.class, GrizzlyBears.class})
class TreeMonkeyTest extends BaseCardTest {

    @Test
    @DisplayName("Tree Monkey can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent treeMonkey = addCreatureReady(player2, new TreeMonkey());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(treeMonkey.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without reach cannot block a creature with flying")
    void creatureWithoutReachCannotBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tree Monkey can also block a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent treeMonkey = addCreatureReady(player2, new TreeMonkey());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(treeMonkey.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach does not prevent a creature without flying from blocking Tree Monkey")
    void canBeBlockedByNonFlyingCreature() {
        addCreatureReady(player1, new TreeMonkey());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped Tree Monkey cannot block a creature with flying")
    void tappedTreeMonkeyCannotBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent treeMonkey = addCreatureReady(player2, new TreeMonkey());
        treeMonkey.tap();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(treeMonkey.isBlocking()).isFalse();
    }
}
