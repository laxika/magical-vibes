package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Varchild's Crusader")
class VarchildsCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Wall creature can't block the Crusader after activation")
    void nonWallCreatureCannotBlock() {
        activateCrusader();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Walls");
    }

    @Test
    @DisplayName("A Wall can still block the Crusader")
    void wallCanBlock() {
        activateCrusader();

        Permanent wall = new Permanent(new WallOfFire());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The Crusader is sacrificed at the beginning of the next end step")
    void sacrificedAtNextEndStep() {
        Permanent crusader = activateCrusader();
        crusader.setAttacking(false);

        harness.assertOnBattlefield(player1, "Varchild's Crusader");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Varchild's Crusader");
        harness.assertInGraveyard(player1, "Varchild's Crusader");
    }

    /**
     * Puts the Crusader onto the battlefield, activates its {@code {0}} ability and resolves it,
     * leaving the Crusader attacking.
     */
    private Permanent activateCrusader() {
        Permanent crusader = new Permanent(new VarchildsCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crusader);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        crusader.setAttacking(true);
        return crusader;
    }
}
