package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.j.JibbirikOmnivore;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FarFortuneEndBoss.class, ColossalDreadmaw.class, JibbirikOmnivore.class})
class FarFortuneEndBossTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to each opponent")
    void attackingDealsDamageToEachOpponent() {
        addCreatureReady(player1, new FarFortuneEndBoss());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("At max speed, damage to an opponent is increased by 1")
    void maxSpeedIncreasesDamageToOpponent() {
        addCreatureReady(player1, new FarFortuneEndBoss());
        gd.playerSpeeds.put(player1.getId(), 4);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("At max speed, combat damage to an opponent's permanent is increased by 1")
    void maxSpeedIncreasesDamageToOpponentPermanent() {
        Permanent attacker = addCreatureReady(player1, new FarFortuneEndBoss());
        gd.playerSpeeds.put(player1.getId(), 4);
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking with another creature triggers Far Fortune while it stays back")
    void attackingWithAnotherCreatureDealsDamage() {
        addCreatureReady(player1, new FarFortuneEndBoss());
        addCreatureReady(player1, new JibbirikOmnivore());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Attacking with multiple other creatures triggers Far Fortune only once")
    void attackingWithMultipleOtherCreaturesDealsDamageOnce() {
        addCreatureReady(player1, new FarFortuneEndBoss());
        addCreatureReady(player1, new JibbirikOmnivore());
        addCreatureReady(player1, new JibbirikOmnivore());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("Max speed boosts both Far Fortune's trigger and another creature's combat damage")
    void maxSpeedBoostsOtherCreatureDamage() {
        addCreatureReady(player1, new FarFortuneEndBoss());
        addCreatureReady(player1, new JibbirikOmnivore());
        gd.playerSpeeds.put(player1.getId(), 4);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Far Fortune starts speed at 1 on entering the battlefield")
    void enteringStartsEngines() {
        harness.enterBattlefieldAndReturn(player1, new FarFortuneEndBoss());
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Reaching max speed from attack damage boosts subsequent combat damage")
    void attackDamageReachesMaxSpeedBeforeCombatDamage() {
        addCreatureReady(player1, new FarFortuneEndBoss());
        gd.playerSpeeds.put(player1.getId(), 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Max speed does not increase damage dealt by an opponent's creature")
    void maxSpeedDoesNotBoostOpponentDamage() {
        Permanent farFortune = addCreatureReady(player1, new FarFortuneEndBoss());
        addCreatureReady(player2, new JibbirikOmnivore());
        gd.playerSpeeds.put(player1.getId(), 4);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(farFortune.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Entering with existing speed does not reset it")
    void enteringPreservesExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.enterBattlefieldAndReturn(player1, new FarFortuneEndBoss());
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
    }
}
