package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MightOfOldKrosa;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomWurm.class, MightOfOldKrosa.class, SuddenShock.class})
class PhantomWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        harness.castFromHand(player1, new PhantomWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Phantom Wurm");
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Prevents damage and removes one +1/+1 counter")
    void preventsDamageAndRemovesOneCounter() {
        Permanent wurm = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());
        castSuddenShock(wurm);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(wurm.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each separate damage event removes one counter")
    void eachDamageEventRemovesOneCounter() {
        Permanent wurm = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());
        castSuddenShock(wurm);
        castSuddenShock(wurm);

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wurm.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Still prevents damage after all counters are removed")
    void stillPreventsDamageAfterAllCountersAreRemoved() {
        Permanent wurm = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, wurm.getId());

        for (int i = 0; i < 4; i++) {
            castSuddenShock(wurm);
        }

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);

        castSuddenShock(wurm);

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(wurm.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Removes a counter even when damage cannot be prevented")
    void removesCounterWhenDamageCannotBePrevented() {
        Permanent wurm = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());
        gd.damageCantBePreventedThisTurn = true;

        castSuddenShock(wurm);

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(wurm.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Losing the last counter puts an unboosted Wurm into the graveyard")
    void losingLastCounterCausesDeath() {
        Permanent wurm = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());

        for (int i = 0; i < 4; i++) {
            castSuddenShock(wurm);
        }

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(wurm.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Phantom Wurm");
        harness.assertInGraveyard(player2, "Phantom Wurm");
    }

    @Test
    @DisplayName("Combat damage is prevented and removes just one counter")
    void preventsCombatDamage() {
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new PhantomWurm());
        attacker.setSummoningSick(false);
        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @CardUsed(Malignus.class)
    @DisplayName("Unpreventable combat damage from a source still removes one counter")
    void sourceUnpreventableCombatDamageStillRemovesCounter() {
        harness.setLife(player2, 2);
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new Malignus());
        attacker.setSummoningSick(false);
        Permanent wurm = harness.enterBattlefieldAndReturn(player2, new PhantomWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(wurm.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
    }

    private void castSuddenShock(Permanent target) {
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
