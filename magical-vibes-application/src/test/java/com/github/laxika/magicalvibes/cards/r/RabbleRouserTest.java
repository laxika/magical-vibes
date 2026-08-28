package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RabbleRouser.class, GrizzlyBears.class})
class RabbleRouserTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1 puts a +1/+1 counter on it when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castRabbleRouser();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 does not apply when no opponent was dealt damage")
    void bloodthirstDoesNotApplyWithoutOpponentDamage() {
        castRabbleRouser();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castRabbleRouser();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability boosts every attacking creature by the source's power")
    void abilityUsesSourcePowerAndBoostsAllAttackers() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        rouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        rouser.setAttacking(true);
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        Permanent opposingAttacker = addCreatureReady(player2, new GrizzlyBears());
        opposingAttacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        harness.passBothPriorities();

        assertThat(rouser.getPowerModifier()).isEqualTo(2);
        assertThat(ownAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(opposingAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(nonAttacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void abilityBoostWearsOff() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
    }

    private void castRabbleRouser() {
        harness.setHand(player1, java.util.List.of(new RabbleRouser()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
