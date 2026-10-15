package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodOgre.class, RuneclawBear.class})
class BloodOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst adds only one counter regardless of damage amount")
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 5);
        castOgre();

        assertThat(findPermanent(player1, "Blood Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst also applies when entering without being cast")
    void bloodthirstAppliesWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        Permanent ogre = harness.enterBattlefieldAndReturn(player1, new BloodOgre());

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal damage back")
    void firstStrikeKillsBlockerBeforeNormalDamage() {
        Permanent ogre = addCreatureReady(player1, new BloodOgre());
        Permanent bear = addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ogre);
        assertThat(ogre.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertLife(player2, 20);
    }

    private void castOgre() {
        harness.castFromHand(player1, new BloodOgre(), "{2}{R}");
        resolveAllTriggers();
    }
}
