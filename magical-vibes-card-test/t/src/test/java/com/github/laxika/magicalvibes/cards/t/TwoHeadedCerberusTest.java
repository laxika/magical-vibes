package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SatyrRambler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TwoHeadedCerberus.class, SatyrRambler.class, TravelingPhilosopher.class})
class TwoHeadedCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked double strike deals damage in both combat phases")
    void unblockedDoubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCerberus(player1);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Double strike kills a small blocker before it can deal regular damage")
    void doubleStrikeKillsSmallBlockerBeforeRegularDamage() {
        Permanent attacker = addReadyCerberus(player1);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Two-Headed Cerberus");
        harness.assertInGraveyard(player2, "Satyr Rambler");
        harness.assertNotOnBattlefield(player2, "Satyr Rambler");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Double strike deals its second hit to a surviving blocker simultaneously with return damage")
    void doubleStrikeTradesWithTwoToughnessBlocker() {
        Permanent attacker = addReadyCerberus(player1);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Two-Headed Cerberus");
        harness.assertInGraveyard(player2, "Traveling Philosopher");
        harness.assertNotOnBattlefield(player1, "Two-Headed Cerberus");
        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyCerberus(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TwoHeadedCerberus());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
