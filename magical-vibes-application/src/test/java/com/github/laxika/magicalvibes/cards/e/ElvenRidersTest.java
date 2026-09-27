package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.cards.s.SilklashSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfMulch;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
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

@CardUsed({ElvenRiders.class, GoblinSkyRaider.class, SilklashSpider.class, WallOfMulch.class, WirewoodElf.class})
class ElvenRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Elven Riders cannot be blocked by non-Wall non-flying creature")
    void cannotBeBlockedByNormalCreature() {
        addCreatureReady(player1, new ElvenRiders());
        addCreatureReady(player2, new WirewoodElf());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or Walls");
    }

    @Test
    @DisplayName("Elven Riders can be blocked by a Wall")
    void canBeBlockedByWall() {
        addCreatureReady(player1, new ElvenRiders());
        addCreatureReady(player2, new WallOfMulch());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Elven Riders can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new ElvenRiders());
        Permanent flyer = addCreatureReady(player2, new GoblinSkyRaider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach is not enough to block Elven Riders")
    void reachIsNotEnoughToBlock() {
        addCreatureReady(player1, new ElvenRiders());
        addCreatureReady(player2, new SilklashSpider());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or Walls");
    }
}
