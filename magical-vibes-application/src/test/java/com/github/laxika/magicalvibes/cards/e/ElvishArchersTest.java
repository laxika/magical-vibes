package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ElvishArchers.class, GrizzlyBears.class})
class ElvishArchersTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeItDealsCombatDamage() {
        addCreatureReady(player1, new ElvishArchers()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Archers");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("First strike kills a 2/2 attacker before it deals combat damage")
    void firstStrikeKillsAttackerBeforeItDealsCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new ElvishArchers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Elvish Archers");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked first striker deals combat damage only once")
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new ElvishArchers()).setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Elvish Archers");
    }

    @Test
    @DisplayName("Opposing first strikers deal lethal combat damage simultaneously")
    void opposingFirstStrikersDealDamageSimultaneously() {
        addCreatureReady(player1, new ElvishArchers()).setAttacking(true);
        addCreatureReady(player2, new ElvishArchers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Archers");
        harness.assertInGraveyard(player2, "Elvish Archers");
        harness.assertLife(player2, 20);
    }
}
