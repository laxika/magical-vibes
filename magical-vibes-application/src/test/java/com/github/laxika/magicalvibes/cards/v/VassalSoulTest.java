package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VassalSoul.class, AxebaneStag.class, ToweringIndrik.class})
class VassalSoulTest extends BaseCardTest {

    @Test
    void creatureWithoutFlyingOrReachCannotBlock() {
        addCreatureReady(player1, new VassalSoul());
        harness.addToBattlefield(player2, new AxebaneStag());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.assertLife(player2, 18);
    }

    @Test
    void flyingCreatureCanBlockWhileSummoningSick() {
        addCreatureReady(player1, new VassalSoul());
        harness.addToBattlefield(player2, new VassalSoul());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Vassal Soul");
        harness.assertInGraveyard(player2, "Vassal Soul");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new VassalSoul());
        harness.addToBattlefield(player2, new ToweringIndrik());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Vassal Soul");
        harness.assertOnBattlefield(player2, "Towering Indrik");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingDoesNotPreventBlockingGroundCreature() {
        addCreatureReady(player1, new AxebaneStag());
        harness.addToBattlefield(player2, new VassalSoul());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Vassal Soul");
        harness.assertOnBattlefield(player1, "Axebane Stag");
        harness.assertLife(player2, 20);
    }
}
