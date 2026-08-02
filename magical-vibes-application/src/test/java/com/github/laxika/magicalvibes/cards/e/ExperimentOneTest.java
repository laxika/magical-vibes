package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExperimentOneTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Experiment One when a bigger creature enters")
    void evolvesWhenBiggerCreatureEnters() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger for a creature with equal power and toughness")
    void doesNotEvolveForEqualStats() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());

        harness.setHand(player1, List.of(new ExperimentOne()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability removes two +1/+1 counters and grants a regeneration shield")
    void abilityRemovesTwoCountersAndGrantsShield() {
        Permanent experimentOne = addReadyExperimentOne(player1);
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(experimentOne.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability with only one +1/+1 counter")
    void cannotActivateWithOneCounter() {
        Permanent experimentOne = addReadyExperimentOne(player1);
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Regeneration shield saves Experiment One from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent experimentOne = addReadyExperimentOne(player1);
        experimentOne.setRegenerationShield(1);
        experimentOne.setBlocking(true);
        experimentOne.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Experiment One");
        Permanent survivor = findPermanent(player1, "Experiment One");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
    }

    private Permanent addReadyExperimentOne(Player player) {
        Permanent perm = new Permanent(new ExperimentOne());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
