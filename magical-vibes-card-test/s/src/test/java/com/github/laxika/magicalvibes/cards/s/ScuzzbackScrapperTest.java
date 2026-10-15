package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScuzzbackScrapper.class, GrizzlyBears.class})
class ScuzzbackScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Wither: combat damage to a blocking creature is dealt as -1/-1 counters")
    void witherDealsMinusCountersToBlocker() {
        // Grizzly Bears (2/2) blocks
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        // Scuzzback Scrapper (1/1) attacks
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrapper());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Scrapper (1/1) takes 2 damage and dies
        harness.assertInGraveyard(player1, "Scuzzback Scrapper");

        // Grizzly Bears survives as a 1/1 with a single -1/-1 counter (not 2 damage marked)
        Permanent survivor = gd.playerBattlefields.get(player2.getId()).get(0);
        assertThat(survivor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wither does not affect players: unblocked damage is normal life loss")
    void witherUnblockedDealsNormalLifeLoss() {
        harness.setLife(player2, 20);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrapper());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Wither also puts counters on an attacker when Scrapper blocks")
    void witherDealsMinusCountersWhileBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new ScuzzbackScrapper());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scuzzback Scrapper");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Simultaneous wither damage kills both Scrappers")
    void simultaneousWitherDamageKillsBothCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrapper());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new ScuzzbackScrapper());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scuzzback Scrapper");
        harness.assertInGraveyard(player2, "Scuzzback Scrapper");
        harness.assertNotOnBattlefield(player1, "Scuzzback Scrapper");
        harness.assertNotOnBattlefield(player2, "Scuzzback Scrapper");
        harness.assertLife(player2, 20);
    }
}
