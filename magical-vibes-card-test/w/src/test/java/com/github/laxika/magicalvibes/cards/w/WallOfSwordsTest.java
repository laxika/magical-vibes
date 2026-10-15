package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfSwords.class, AirElemental.class, GrizzlyBears.class})
class WallOfSwordsTest extends BaseCardTest {

    @Test
    void defenderPreventsAttacking() {
        Permanent wall = addCreatureReady(player1, new WallOfSwords());
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isAttacking()).isFalse();
    }

    @Test
    void flyingAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent wall = addCreatureReady(player2, new WallOfSwords());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    void nonFlyingCreatureCannotBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfSwords());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    void summoningSickWallCanBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    void tappedWallCannotBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent wall = addCreatureReady(player2, new WallOfSwords());
        wall.tap();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isBlocking()).isFalse();
    }
}
