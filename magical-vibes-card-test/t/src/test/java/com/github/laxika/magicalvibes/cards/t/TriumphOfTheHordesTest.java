package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphOfTheHordes.class, GrizzlyBears.class})
class TriumphOfTheHordesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives own creatures +1/+1, trample, and infect")
    void resolvesAndBuffsOwnCreatures() {
        Permanent p1a = addCreatureReady(player1, new GrizzlyBears());
        Permanent p1b = addCreatureReady(player1, new GrizzlyBears());
        Permanent p2 = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, p1a)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, p1a)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, p1b)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, p1b)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, p1a, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, p1b, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, p1a, Keyword.INFECT)).isTrue();
        assertThat(gqs.hasKeyword(gd, p1b, Keyword.INFECT)).isTrue();

        assertThat(gqs.getEffectivePower(gd, p2)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, p2)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, p2, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, p2, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Infect creature deals poison counters to defending player via trample")
    void infectDealsPoisonCountersViaTrample() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Assign lethal damage to the 2/2 blocker and excess damage to the player as poison.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        // Infect damage to player = poison counters, not life loss
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(TriumphOfTheHordes.class);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the effects")
    void laterCreaturesAreNotAffected() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent newcomer = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Creatures present at resolution receive the effects even if absent when cast")
    void creaturesAreSelectedAtResolution() {
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Granted infect deals persistent minus counters to creatures")
    void infectCountersPersistAfterTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
    }
}
