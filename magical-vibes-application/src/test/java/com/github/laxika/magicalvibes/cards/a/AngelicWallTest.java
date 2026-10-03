package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicWall.class, DuskImp.class})
class AngelicWallTest extends BaseCardTest {

    @Test
    void defenderPreventsAttacking() {
        Permanent wall = addCreatureReady(player1, new AngelicWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(wall.isAttacking()).isFalse();
    }

    @Test
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new DuskImp());
        Permanent wall = addCreatureReady(player2, new AngelicWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    void summoningSickWallCanBlockAndSurviveFlyingAttacker() {
        addCreatureReady(player1, new DuskImp());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Angelic Wall");
        harness.assertOnBattlefield(player1, "Dusk Imp");
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }
}
