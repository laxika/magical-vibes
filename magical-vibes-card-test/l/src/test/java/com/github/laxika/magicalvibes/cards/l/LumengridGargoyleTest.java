package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Blightwidow;
import com.github.laxika.magicalvibes.cards.s.SpinEngine;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumengridGargoyle.class, SpinEngine.class, Blightwidow.class})
class LumengridGargoyleTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new LumengridGargoyle());
        addCreatureReady(player2, new SpinEngine());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player2, "Spin Engine");
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new LumengridGargoyle());
        addCreatureReady(player2, new LumengridGargoyle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Lumengrid Gargoyle");
        harness.assertInGraveyard(player2, "Lumengrid Gargoyle");
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new LumengridGargoyle());
        addCreatureReady(player2, new Blightwidow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Lumengrid Gargoyle");
        harness.assertInGraveyard(player2, "Blightwidow");
    }

    @Test
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new SpinEngine());
        addCreatureReady(player2, new LumengridGargoyle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Spin Engine");
        harness.assertOnBattlefield(player2, "Lumengrid Gargoyle");
    }
}
