package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(pyromancer), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Only the next 1 damage is prevented")
    void preventsOnlyOneCombatDamage() {
        Permanent creature = castUrsineFylgja();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
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

    private Permanent castUrsineFylgja() {
        harness.setHand(player1, List.of(new UrsineFylgja()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UrsineFylgja)
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
