package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmrouKithkin;
import com.github.laxika.magicalvibes.cards.w.WallOfCaltrops;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarblePriest.class, WallOfEarth.class, WallOfCaltrops.class, AmrouKithkin.class})
class MarblePriestTest extends BaseCardTest {

    @Test
    @DisplayName("All able Walls must block Marble Priest, but other creatures are not forced")
    void allAbleWallsMustBlockButOtherCreaturesAreNotForced() {
        addCreatureReady(player1, new MarblePriest());
        addCreatureReady(player2, new WallOfEarth());
        addCreatureReady(player2, new WallOfEarth());
        addCreatureReady(player2, new AmrouKithkin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(2).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A Wall that cannot block is not required to block Marble Priest")
    void onlyAbleWallsMustBlock() {
        addCreatureReady(player1, new MarblePriest());
        Permanent tappedWall = addCreatureReady(player2, new WallOfEarth());
        tappedWall.tap();
        Permanent ableWall = addCreatureReady(player2, new WallOfEarth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(tappedWall.isBlocking()).isFalse();
        assertThat(ableWall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage from Walls is prevented, but combat damage from other creatures is not")
    void preventsCombatDamageFromWallsOnly() {
        Permanent priest = addCreatureReady(player1, new MarblePriest());
        Permanent wall = addCreatureReady(player2, new WallOfCaltrops());
        addCreatureReady(player2, new AmrouKithkin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(wall.getId(), 3));

        assertThat(priest.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(priest);
    }
}
