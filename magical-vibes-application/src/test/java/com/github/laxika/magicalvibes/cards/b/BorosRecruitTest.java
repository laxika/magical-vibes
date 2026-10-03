package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosRecruit.class, ElvesOfDeepShadow.class})
class BorosRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Boros Recruit destroy a blocking creature before it deals combat damage")
    void firstStrikeDealsDamageBeforeBlocker() {
        Permanent recruit = addCreatureReady(player1, new BorosRecruit());
        Permanent blocker = addCreatureReady(player2, new ElvesOfDeepShadow());
        recruit.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(recruit);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A blocking Boros Recruit destroys a normal attacker before it can deal damage")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new ElvesOfDeepShadow());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        attacker.setAttacking(true);
        recruit.setBlocking(true);
        recruit.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(recruit);
    }

    @Test
    @DisplayName("Opposing Boros Recruits deal first-strike damage simultaneously and both die")
    void opposingFirstStrikersKillEachOther() {
        Permanent attacker = addCreatureReady(player1, new BorosRecruit());
        Permanent blocker = addCreatureReady(player2, new BorosRecruit());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("An unblocked Boros Recruit deals damage only once")
    void firstStrikeDoesNotDealRegularCombatDamage() {
        Permanent recruit = addCreatureReady(player1, new BorosRecruit());
        recruit.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 19);
    }
}
