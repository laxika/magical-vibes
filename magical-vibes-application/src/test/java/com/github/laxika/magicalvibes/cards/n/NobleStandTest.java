package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.b.BolaWarrior;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({NobleStand.class, DefiantFalcon.class, BolaWarrior.class, WallOfGlare.class})
class NobleStandTest extends BaseCardTest {

    @Test
    @DisplayName("You gain 2 life whenever a creature you control blocks")
    void ownCreatureBlocksGainsLife() {
        addCreatureReady(player2, new DefiantFalcon());
        addCreatureReady(player1, new DefiantFalcon());
        addCreatureReady(player1, new NobleStand());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's blocking creature does not trigger Noble Stand")
    void opponentCreatureBlocksDoesNotGainLife() {
        addCreatureReady(player1, new DefiantFalcon());
        addCreatureReady(player2, new DefiantFalcon());
        addCreatureReady(player1, new NobleStand());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each creature that blocks triggers Noble Stand separately")
    void eachBlockingCreatureTriggersSeparately() {
        addCreatureReady(player2, new DefiantFalcon());
        addCreatureReady(player2, new DefiantFalcon());
        addCreatureReady(player1, new DefiantFalcon());
        addCreatureReady(player1, new DefiantFalcon());
        addCreatureReady(player1, new NobleStand());

        declareAttackers(player2, List.of(0, 1));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A creature blocking multiple attackers triggers Noble Stand only once")
    void oneBlockingCreatureTriggersOnlyOnce() {
        addCreatureReady(player2, new BolaWarrior());
        addCreatureReady(player2, new BolaWarrior());
        addCreatureReady(player1, new WallOfGlare());
        addCreatureReady(player1, new NobleStand());

        declareAttackers(player2, List.of(0, 1));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Noble Stand does not trigger when no creature blocks")
    void noBlocksNoLifeGain() {
        addCreatureReady(player2, new DefiantFalcon());
        addCreatureReady(player1, new DefiantFalcon());
        addCreatureReady(player1, new NobleStand());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }
}
