package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraniticTitan;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ScroungerOfSouls.class, GraniticTitan.class})
class ScroungerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking a player gains controller life equal to combat damage dealt")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScroungerOfSouls());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Lifelink gains life from damage to a blocker even when Scrounger dies")
    void lifelinkGainsLifeWhenBlockedAndKilled() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScroungerOfSouls());
        addCreatureReady(player2, new GraniticTitan());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Scrounger of Souls");
        harness.assertOnBattlefield(player2, "Granitic Titan");
    }

    @Test
    @DisplayName("Attacking and blocking Scroungers each gain life for their own controller")
    void lifelinkGainsLifeForBlockingController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScroungerOfSouls());
        addCreatureReady(player2, new ScroungerOfSouls());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertOnBattlefield(player1, "Scrounger of Souls");
        harness.assertOnBattlefield(player2, "Scrounger of Souls");
    }
}
