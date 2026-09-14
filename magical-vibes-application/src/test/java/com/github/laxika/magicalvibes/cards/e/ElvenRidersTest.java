package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenRiders.class, AirElemental.class, GiantSpider.class, GrizzlyBears.class, WallOfFire.class})
class ElvenRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Elven Riders cannot be blocked by non-Wall non-flying creature")
    void cannotBeBlockedByNormalCreature() {
        attackingRiders();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or Walls");
    }

    @Test
    @DisplayName("Elven Riders can be blocked by a Wall")
    void canBeBlockedByWall() {
        attackingRiders();
        addCreatureReady(player2, new WallOfFire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Elven Riders can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        attackingRiders();
        Permanent flyer = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach is not enough to block Elven Riders")
    void reachIsNotEnoughToBlock() {
        attackingRiders();
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or Walls");
    }

    private void attackingRiders() {
        Permanent riders = addCreatureReady(player1, new ElvenRiders());
        riders.setAttacking(true);
    }
}
