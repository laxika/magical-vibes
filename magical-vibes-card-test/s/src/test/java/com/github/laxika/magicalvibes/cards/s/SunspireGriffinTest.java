package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunspireGriffin.class, AxebaneStag.class, ToweringIndrik.class})
class SunspireGriffinTest extends BaseCardTest {

    @Test
    void creatureWithoutFlyingOrReachCannotBlock() {
        addCreatureReady(player1, new SunspireGriffin());
        harness.addToBattlefield(player2, new AxebaneStag());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockWhileSummoningSick() {
        addCreatureReady(player1, new SunspireGriffin());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SunspireGriffin());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertOnBattlefield(player1, "Sunspire Griffin");
        harness.assertOnBattlefield(player2, "Sunspire Griffin");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new SunspireGriffin());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ToweringIndrik());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertOnBattlefield(player1, "Sunspire Griffin");
        harness.assertOnBattlefield(player2, "Towering Indrik");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingDoesNotPreventBlockingGroundCreature() {
        addCreatureReady(player1, new AxebaneStag());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SunspireGriffin());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertInGraveyard(player2, "Sunspire Griffin");
        harness.assertOnBattlefield(player1, "Axebane Stag");
        harness.assertLife(player2, 20);
    }
}
