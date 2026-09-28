package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SliverOfHope.class, StrikingSliver.class, GrizzlyBears.class, Shock.class})
class SliverOfHopeTest extends BaseCardTest {

    @Test
    void grantsHopeToSliversYouControl() {
        harness.addToBattlefield(player1, new SliverOfHope());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new StrikingSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HOPE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.HOPE)).isFalse();
    }

    @Test
    void preventsAllDamageToAttackingSlivers() {
        harness.addToBattlefield(player1, new SliverOfHope());
        Permanent attacker = addCreatureReady(player1, new StrikingSliver());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
