package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiveAlarmFireTest extends BaseCardTest {

    private Permanent addFire() {
        Permanent perm = new Permanent(new FiveAlarmFire());
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card, com.github.laxika.magicalvibes.model.Player owner) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Blaze counter is added when a creature you control deals combat damage to a player")
    void blazeCounterOnDamageToPlayer() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // Five-Alarm Fire trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blaze counter is added when the damage is dealt only to a blocking creature")
    void blazeCounterOnDamageToCreature() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);

        Permanent blocker = addReadyCreature(new SerraAngel(), player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // Five-Alarm Fire trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature an opponent controls dealing combat damage does not add a blaze counter")
    void opponentCreatureDoesNotTrigger() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player2).setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isZero();
    }

    @Test
    @DisplayName("Removing five blaze counters deals 5 damage to any target")
    void removeFiveCountersDealsFiveDamage() {
        Permanent fire = addFire();
        fire.setCounterCount(CounterType.BLAZE, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isZero();
    }

    @Test
    @DisplayName("The ability can't be activated with fewer than five blaze counters")
    void cannotActivateWithoutFiveCounters() {
        Permanent fire = addFire();
        fire.setCounterCount(CounterType.BLAZE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(4);
    }
}
