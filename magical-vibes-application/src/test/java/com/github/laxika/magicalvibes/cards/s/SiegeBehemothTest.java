package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiegeBehemoth.class, GrizzlyBears.class, RagingGoblin.class})
class SiegeBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked Siege Behemoth may assign combat damage to the defending player")
    void blockedSiegeBehemothMayAssignDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        behemoth.setAttacking(true);
        behemoth.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(behemoth.getId());

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Siege Behemoth also grants the option to another attacking creature")
    void grantsOptionToAnotherCreature() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        behemoth.setAttacking(true);
        behemoth.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        blocker.addBlockingTargetId(attacker.getId());

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 1, Map.of(player2.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the option assigns damage to the blocker normally")
    void decliningUsesNormalBlockedAssignment() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        behemoth.setAttacking(true);
        behemoth.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(behemoth.getId());

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("The ability does not apply while Siege Behemoth is not attacking")
    void doesNotApplyWhileNotAttacking() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new SiegeBehemoth());
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        blocker.addBlockingTargetId(attacker.getId());

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(behemoth);
    }
}
