package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.p.PrescientChimera;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoneshockGiant.class, NessianCourser.class, PrescientChimera.class})
class StoneshockGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Stoneshock Giant")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent giant = addReadyStoneshockGiant(player1);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(giant.isMonstrous()).isTrue();
        assertThat(giant.getEffectivePower()).isEqualTo(8);
        assertThat(giant.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Becoming monstrous stops opponents' non-flying creatures from blocking this turn")
    void becomingMonstrousRestrictsOpponentsNonFliers() {
        Permanent giant = addReadyStoneshockGiant(player1);
        Permanent ownBears = addCreatureReady(player1, new NessianCourser());
        Permanent opponentBears = addCreatureReady(player2, new NessianCourser());
        Permanent opponentAirElemental = addCreatureReady(player2, new PrescientChimera());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(giant.isMonstrous()).isTrue();
        assertThat(bls.canBlockAttacker(gd, ownBears, opponentBears,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, opponentBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, opponentAirElemental, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing once monstrous")
    void monstrosityOnlyResolvesOnce() {
        Permanent giant = addReadyStoneshockGiant(player1);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(giant.isMonstrous()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blocking restriction affects later creatures and expires next turn")
    void blockingRestrictionAffectsLaterCreaturesAndExpires() {
        Permanent giant = addReadyStoneshockGiant(player1);
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        Permanent lateCreature = addCreatureReady(player2, new NessianCourser());
        assertThat(bls.canBlockAttacker(gd, lateCreature, giant,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bls.canBlockAttacker(gd, lateCreature, giant,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Monstrosity does nothing if its source leaves before resolution")
    void sourceLeavingPreventsMonstrosityAndTrigger() {
        Permanent giant = addReadyStoneshockGiant(player1);
        Permanent opponent = addCreatureReady(player2, new NessianCourser());
        Permanent attacker = addCreatureReady(player1, new NessianCourser());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        resolveAllTriggers();

        assertThat(giant.isMonstrous()).isFalse();
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bls.canBlockAttacker(gd, opponent, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private Permanent addReadyStoneshockGiant(Player player) {
        return addCreatureReady(player, new StoneshockGiant());
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
