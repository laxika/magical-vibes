package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WakestoneGargoyle;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RollingStones.class, WallOfAir.class, WakestoneGargoyle.class, GrizzlyBears.class})
class RollingStonesTest extends BaseCardTest {

    @Test
    @DisplayName("Wall cannot attack without Rolling Stones (defender)")
    void wallCannotAttackWithoutRollingStones() {
        addCreatureReady(player1, new WallOfAir());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall can attack while its controller has Rolling Stones")
    void wallCanAttackWithRollingStones() {
        Permanent wall = addCreatureReady(player1, new WallOfAir());
        harness.addToBattlefield(player1, new RollingStones());
        addCreatureReady(player2, new GrizzlyBears());
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        declareAttackersAndPrepareBlockers(List.of(wallIndex));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Rolling Stones affects Wall creatures globally, even under another player")
    void wallCanAttackWhenOpponentControlsRollingStones() {
        Permanent wall = addCreatureReady(player1, new WallOfAir());
        harness.addToBattlefield(player2, new RollingStones());
        addCreatureReady(player2, new GrizzlyBears());
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        declareAttackersAndPrepareBlockers(List.of(wallIndex));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Rolling Stones does not let a non-Wall defender creature attack")
    void nonWallDefenderCannotAttackWithRollingStones() {
        Permanent gargoyle = addCreatureReady(player1, new WakestoneGargoyle());
        harness.addToBattlefield(player1, new RollingStones());

        int gargoyleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gargoyle);
        assertThatThrownBy(() -> declareAttackers(List.of(gargoyleIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall cannot attack after Rolling Stones leaves the battlefield")
    void wallCannotAttackAfterRollingStonesRemoved() {
        Permanent wall = addCreatureReady(player1, new WallOfAir());
        Permanent rollingStones = harness.addToBattlefieldAndReturn(player1, new RollingStones());

        gd.playerBattlefields.get(player1.getId()).remove(rollingStones);
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        assertThatThrownBy(() -> declareAttackers(List.of(wallIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Rolling Stones does not bypass summoning sickness")
    void summoningSickWallCannotAttack() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfAir());
        wall.setSummoningSick(true);
        harness.addToBattlefield(player1, new RollingStones());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Rolling Stones does not let a tapped Wall attack")
    void tappedWallCannotAttack() {
        Permanent wall = addCreatureReady(player1, new WallOfAir());
        wall.tap();
        harness.addToBattlefield(player1, new RollingStones());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A Wall retains defender while attacking with Rolling Stones")
    void attackingWallRetainsDefender() {
        Permanent wall = addCreatureReady(player1, new WallOfAir());
        harness.addToBattlefield(player1, new RollingStones());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("A remaining Rolling Stones still lets Walls attack after another copy leaves")
    void remainingCopyStillAllowsWallToAttack() {
        Permanent wall = addCreatureReady(player1, new WallOfAir());
        Permanent firstCopy = harness.addToBattlefieldAndReturn(player1, new RollingStones());
        harness.addToBattlefield(player2, new RollingStones());
        addCreatureReady(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).remove(firstCopy);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
    }
}
