package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SliverOfHope.class, StrikingSliver.class, GrizzlyBears.class, Shock.class})
class SliverOfHopeTest extends BaseCardTest {

    @Test
    void grantsHopeToSliversYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SliverOfHope());
        Permanent opponentSliver = harness.addToBattlefieldAndReturn(player2, new StrikingSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new StrikingSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, source, Keyword.HOPE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HOPE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HOPE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.HOPE)).isFalse();
    }

    @Test
    void protectsItselfWhileAttacking() {
        Permanent source = addCreatureReady(player1, new SliverOfHope());
        source.setAttacking(true);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sliver of Hope");
    }

    @Test
    void hopeDoesNotPreventOutgoingCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new SliverOfHope());
        Permanent blocker = addCreatureReady(player2, new SliverOfHope());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Sliver of Hope");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Sliver of Hope");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    void grantEndsWhenSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SliverOfHope());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new StrikingSliver());
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HOPE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HOPE)).isFalse();
    }

    @Test
    void preventsAllDamageToAttackingSlivers() {
        harness.addToBattlefield(player1, new SliverOfHope());
        Permanent attacker = addCreatureReady(player1, new StrikingSliver());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void preventsNoncombatDamageToAttackingSlivers() {
        harness.addToBattlefield(player1, new SliverOfHope());
        Permanent attacker = addCreatureReady(player1, new StrikingSliver());
        attacker.setAttacking(true);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotPreventDamageToNonattackingSlivers() {
        harness.addToBattlefield(player1, new SliverOfHope());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new StrikingSliver());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, sliver.getId());
        harness.passBothPriorities();

        assertThat(sliver.getMarkedDamage()).isEqualTo(2);
    }
}
