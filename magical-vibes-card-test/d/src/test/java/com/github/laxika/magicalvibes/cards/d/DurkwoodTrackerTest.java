package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FoeRazerRegent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DurkwoodTracker.class, AshcoatBear.class, FoeRazerRegent.class})
class DurkwoodTrackerTest extends BaseCardTest {

    @Test
    void fightsTargetAttackingCreature() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(tracker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new DurkwoodTracker());
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingIfTrackerLeavesBeforeResolution() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tracker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tracker);
        assertThat(tracker.getMarkedDamage()).isZero();
    }

    @Test
    void doesNothingIfTargetStopsAttackingBeforeResolution() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(tracker.getMarkedDamage()).isZero();
    }

    @Test
    void damageExchangeDoesNotTriggerFightAbilities() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        harness.addToBattlefield(player1, new FoeRazerRegent());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bothCreaturesDealDamageEvenWhenBothReceiveLethalDamage() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addCreatureReady(player2, new DurkwoodTracker());
        attacker.setAttacking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tracker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    void usesBothCreaturesCurrentPowerAtResolution() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        tracker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tracker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    void canTargetAnAttackingCreatureYouControlAndPaysTapCost() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(tracker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tracker).doesNotContain(attacker);
        assertThat(tracker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        tracker.tap();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        Permanent tracker = addCreatureReady(player1, new DurkwoodTracker());
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tracker.isTapped()).isFalse();
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new AshcoatBear());
        creature.setAttacking(true);
        return creature;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
