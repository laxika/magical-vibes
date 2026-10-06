package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SacredPrey.class, FreshVolunteers.class})
class SacredPreyTest extends BaseCardTest {

    @Test
    @DisplayName("When Sacred Prey becomes blocked, its controller gains 1 life")
    void becomesBlockedGainsLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SacredPrey());
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures causes only one life gain")
    void multipleBlockersGainLifeOnlyOnce() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SacredPrey());
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An unblocked Sacred Prey does not gain life")
    void unblockedDoesNotGainLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SacredPrey());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Sacred Prey controlled by the opposing player gains life for that player")
    void opposingControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player2, new SacredPrey());
        addCreatureReady(player1, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Each blocked Sacred Prey triggers separately")
    void twoBlockedPreysEachGainLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SacredPrey());
        addCreatureReady(player1, new SacredPrey());
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Sacred Prey does not gain life when it blocks")
    void blockingDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FreshVolunteers());
        addCreatureReady(player2, new SacredPrey());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
