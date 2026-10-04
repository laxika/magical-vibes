package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.CrimsonKobolds;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornetCobra.class, CrimsonKobolds.class, BarbaryApes.class})
class HornetCobraTest extends BaseCardTest {

    @Test
    void firstStrikeKillsAOneOneBeforeItDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new HornetCobra());
        attacker.setAttacking(true);

        CrimsonKobolds blockerCard = new CrimsonKobolds();
        blockerCard.setPower(1);
        blockerCard.setToughness(1);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Crimson Kobolds");
    }

    @Test
    void firstStrikeKillsATwoTwoAttackerBeforeItDamagesTheBlocker() {
        Permanent attacker = addCreatureReady(player1, new BarbaryApes());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HornetCobra());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Barbary Apes");
        harness.assertOnBattlefield(player2, "Hornet Cobra");
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingFirstStrikersDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new HornetCobra());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HornetCobra());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Hornet Cobra");
        harness.assertInGraveyard(player2, "Hornet Cobra");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new HornetCobra());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Hornet Cobra");
    }

    @Test
    void killingTheBlockerDoesNotDealDamageToTheDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new HornetCobra());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Hornet Cobra");
        harness.assertInGraveyard(player2, "Barbary Apes");
        harness.assertLife(player2, 20);
    }
}
