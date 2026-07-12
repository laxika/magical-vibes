package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RollingStonesTest extends BaseCardTest {

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }

    @Test
    @DisplayName("Wall cannot attack without Rolling Stones (defender)")
    void wallCannotAttackWithoutRollingStones() {
        harness.addToBattlefield(player1, new AngelicWall());
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        wall.setSummoningSick(false);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall can attack while its controller has Rolling Stones")
    void wallCanAttackWithRollingStones() {
        harness.addToBattlefield(player1, new AngelicWall());
        harness.addToBattlefield(player1, new RollingStones());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angelic Wall"))
                .findFirst().orElseThrow();
        wall.setSummoningSick(false);
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(wallIndex));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Rolling Stones affects Wall creatures globally, even under another player")
    void wallCanAttackWhenOpponentControlsRollingStones() {
        harness.addToBattlefield(player1, new AngelicWall());
        harness.addToBattlefield(player2, new RollingStones());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        wall.setSummoningSick(false);

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Wall cannot attack after Rolling Stones leaves the battlefield")
    void wallCannotAttackAfterRollingStonesRemoved() {
        harness.addToBattlefield(player1, new AngelicWall());
        harness.addToBattlefield(player1, new RollingStones());
        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angelic Wall"))
                .findFirst().orElseThrow();
        wall.setSummoningSick(false);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Rolling Stones"));
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(wallIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
