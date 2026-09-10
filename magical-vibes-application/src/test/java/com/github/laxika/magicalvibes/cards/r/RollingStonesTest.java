package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WakestoneGargoyle;
import com.github.laxika.magicalvibes.cards.w.WallOfRazors;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RollingStones.class, WallOfRazors.class, WakestoneGargoyle.class, YouthfulKnight.class})
class RollingStonesTest extends BaseCardTest {

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Wall cannot attack without Rolling Stones (defender)")
    void wallCannotAttackWithoutRollingStones() {
        addCreatureReady(player1, new WallOfRazors());

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall can attack while its controller has Rolling Stones")
    void wallCanAttackWithRollingStones() {
        Permanent wall = addCreatureReady(player1, new WallOfRazors());
        harness.addToBattlefield(player1, new RollingStones());
        addCreatureReady(player2, new YouthfulKnight());
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        declareAttackers(List.of(wallIndex));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Rolling Stones affects Wall creatures globally, even under another player")
    void wallCanAttackWhenOpponentControlsRollingStones() {
        Permanent wall = addCreatureReady(player1, new WallOfRazors());
        harness.addToBattlefield(player2, new RollingStones());
        addCreatureReady(player2, new YouthfulKnight());
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        declareAttackers(List.of(wallIndex));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Rolling Stones does not let a non-Wall defender creature attack")
    void nonWallDefenderCannotAttackWithRollingStones() {
        Permanent gargoyle = addCreatureReady(player1, new WakestoneGargoyle());
        harness.addToBattlefield(player1, new RollingStones());

        beginAttackers();

        int gargoyleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gargoyle);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(gargoyleIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall cannot attack after Rolling Stones leaves the battlefield")
    void wallCannotAttackAfterRollingStonesRemoved() {
        Permanent wall = addCreatureReady(player1, new WallOfRazors());
        Permanent rollingStones = harness.addToBattlefieldAndReturn(player1, new RollingStones());

        gd.playerBattlefields.get(player1.getId()).remove(rollingStones);
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(wallIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
