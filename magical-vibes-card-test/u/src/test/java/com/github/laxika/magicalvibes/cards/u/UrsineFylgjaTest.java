package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.cards.l.LightningStorm;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrsineFylgja.class, LightningStorm.class, KrovikanScoundrel.class})
class UrsineFylgjaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four healing counters")
    void entersWithFourHealingCounters() {
        Permanent creature = castUrsineFylgja();

        assertThat(creature.getCounterCount(CounterType.HEALING)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing a healing counter shields itself for 1 damage")
    void removeCounterShieldsItself() {
        Permanent creature = castUrsineFylgja();

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.HEALING)).isEqualTo(3);
        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents the next 1 noncombat damage to itself")
    void preventsNoncombatDamage() {
        Permanent creature = castUrsineFylgja();
        harness.setHand(player1, List.of(new LightningStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(creature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Multiple prevention activations stack")
    void multiplePreventionActivationsStack() {
        Permanent creature = castUrsineFylgja();
        harness.setHand(player1, List.of(new LightningStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.HEALING)).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Only the next 1 damage is prevented")
    void preventsOnlyOneCombatDamage() {
        Permanent creature = castUrsineFylgja();
        Permanent attacker = addCreatureReady(player2, new KrovikanScoundrel());

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(creature), 0)));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot remove a healing counter when none remain")
    void cannotActivateWithoutHealingCounters() {
        Permanent creature = castUrsineFylgja();
        creature.setCounterCount(CounterType.HEALING, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(creature), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{2}{W} puts a healing counter on this creature")
    void manaAbilityAddsHealingCounter() {
        Permanent creature = castUrsineFylgja();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(creature), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.HEALING)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prevention shield clears at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent creature = castUrsineFylgja();

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Healing counter is paid immediately, but prevention waits for resolution")
    void counterIsPaidBeforePreventionResolves() {
        Permanent creature = castUrsineFylgja();
        creature.tap();

        harness.activateAbility(player1, indexOf(creature), 0, null, null);

        assertThat(creature.getCounterCount(CounterType.HEALING)).isEqualTo(3);
        assertThat(creature.getDamagePreventionShield()).isZero();

        harness.passBothPriorities();

        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Replenishing counters uses the stack and enables prevention again")
    void replenishingCountersEnablesPreventionAgain() {
        Permanent creature = castUrsineFylgja();
        creature.setCounterCount(CounterType.HEALING, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(creature), 1, null, null);
        assertThat(creature.getCounterCount(CounterType.HEALING)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.HEALING)).isEqualTo(1);

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.HEALING)).isZero();
        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Self prevention does not protect its controller or consume the shield")
    void preventionDoesNotProtectController() {
        Permanent creature = castUrsineFylgja();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new LightningStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, lifeBefore - 3);
        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent castUrsineFylgja() {
        harness.castFromHand(player1, new UrsineFylgja(), "{4}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Ursine Fylgja");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
