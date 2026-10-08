package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.t.TalonTrooper;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfDenial.class, TalonTrooper.class, GrizzledLeotau.class, Terminate.class})
class WallOfDenialTest extends BaseCardTest {

    @Test
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new WallOfDenial());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void canBlockFlyingCreatureWhileSummoningSick() {
        addCreatureReady(player1, new TalonTrooper());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfDenial());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
        resolveCombat();
        harness.assertOnBattlefield(player2, "Wall of Denial");
        harness.assertOnBattlefield(player1, "Talon Trooper");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new GrizzledLeotau());
        Permanent wall = addCreatureReady(player2, new WallOfDenial());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
        resolveCombat();
        harness.assertOnBattlefield(player2, "Wall of Denial");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void shroudPreventsOpponentTargeting() {
        assertShroudPreventsTargeting(player2);
    }

    @Test
    void shroudPreventsControllerTargeting() {
        assertShroudPreventsTargeting(player1);
    }

    private void assertShroudPreventsTargeting(Player wallController) {
        Permanent wall = harness.addToBattlefieldAndReturn(wallController, new WallOfDenial());
        harness.addToBattlefield(player2, new GrizzledLeotau());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        harness.assertOnBattlefield(wallController, "Wall of Denial");
        assertThat(gd.stack).isEmpty();
    }
}
