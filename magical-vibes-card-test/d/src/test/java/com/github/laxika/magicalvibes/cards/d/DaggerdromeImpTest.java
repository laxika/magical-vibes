package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TowerDrake;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaggerdromeImp.class, DeadReveler.class, TowerDrake.class})
class DaggerdromeImpTest extends BaseCardTest {

    @Test
    void unblockedDamageGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaggerdromeImp());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void creatureWithoutFlyingOrReachCannotBlock() {
        addCreatureReady(player1, new DaggerdromeImp());
        harness.addToBattlefield(player2, new DeadReveler());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingBlockerTradesAndImpStillGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaggerdromeImp());
        harness.addToBattlefield(player2, new TowerDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Daggerdrome Imp");
        harness.assertInGraveyard(player2, "Tower Drake");
        harness.assertNotOnBattlefield(player1, "Daggerdrome Imp");
        harness.assertNotOnBattlefield(player2, "Tower Drake");
    }

    @Test
    void blockingGroundCreatureGainsLifeForDefendingController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DeadReveler());
        harness.addToBattlefield(player2, new DaggerdromeImp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player2, "Daggerdrome Imp");
        harness.assertNotOnBattlefield(player2, "Daggerdrome Imp");
        harness.assertOnBattlefield(player1, "Dead Reveler");
    }

    @Test
    void bothControllersGainLifeWhenImpsTrade() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaggerdromeImp());
        harness.addToBattlefield(player2, new DaggerdromeImp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player1, "Daggerdrome Imp");
        harness.assertInGraveyard(player2, "Daggerdrome Imp");
        harness.assertNotOnBattlefield(player1, "Daggerdrome Imp");
        harness.assertNotOnBattlefield(player2, "Daggerdrome Imp");
    }
}
