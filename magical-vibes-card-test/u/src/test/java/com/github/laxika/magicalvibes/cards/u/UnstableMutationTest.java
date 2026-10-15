package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableMutation.class, GrizzlyBears.class, Forest.class, Disenchant.class})
class UnstableMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Unstable Mutation targeting a creature")
    void castsAndAttachesToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UnstableMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(aura -> aura.isAttached() && aura.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot cast Unstable Mutation targeting a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new UnstableMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creature returns to base stats when Unstable Mutation is removed")
    void boostStopsWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Upkeep -1/-1 counter =====

    @Test
    @DisplayName("At enchanted creature controller's upkeep, a -1/-1 counter is placed on it")
    void upkeepPutsMinusCounter() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(creature.getId());

        int countersBefore = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore + 1);
    }

    @Test
    @DisplayName("Counter trigger does not fire during aura controller's upkeep")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(creature.getId());

        int countersBefore = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore);
    }

    @Test
    @DisplayName("Counters accumulate and shrink the boosted creature over multiple upkeeps")
    void countersAccumulateOverUpkeeps() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        // 2/2 base + 3/3 aura - 2/-2 counters = 3/3
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Destroying the Aura leaves its counters on the creature")
    void countersRemainAfterAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertInGraveyard(player1, "Unstable Mutation");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep trigger still puts a counter on the creature after the Aura is destroyed")
    void pendingTriggerSurvivesAuraDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Disenchant()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Unstable Mutation");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The fifth upkeep counter kills the creature and sends its Aura to the graveyard")
    void accumulatedCountersEventuallyKillCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnstableMutation());
        aura.setAttachedTo(creature.getId());

        for (int upkeep = 0; upkeep < 4; upkeep++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Unstable Mutation");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unstable Mutation");
    }
}
