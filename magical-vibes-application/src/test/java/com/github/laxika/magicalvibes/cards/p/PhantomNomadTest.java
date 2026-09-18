package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomNomad.class, EmberShot.class, FlaringPain.class, SuntailHawk.class})
class PhantomNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.castFromHand(player1, new PhantomNomad(), "{1}{W}");
        harness.passBothPriorities();

        Permanent nomad = findPermanent(player1, "Phantom Nomad");
        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents direct damage and removes one +1/+1 counter")
    void preventsDirectDamageAndRemovesOneCounter() {
        Permanent nomad = addCreatureReady(player2, new PhantomNomad());
        nomad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        dealEmberShot(nomad);

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still prevents damage when no +1/+1 counters remain")
    void preventsDamageWithoutCounters() {
        Permanent nomad = addCreatureReady(player2, new PhantomNomad());
        nomad.setToughnessModifier(1);

        dealEmberShot(nomad);

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nomad.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nomad);
    }

    @Test
    @DisplayName("Unpreventable damage still removes one +1/+1 counter")
    void unpreventableDamageStillRemovesOneCounter() {
        Permanent nomad = addCreatureReady(player2, new PhantomNomad());
        nomad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        nomad.setToughnessModifier(3);

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        dealEmberShot(nomad);

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nomad);
    }

    @Test
    @DisplayName("Prevents combat damage and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounter() {
        Permanent nomad = addCreatureReady(player1, new PhantomNomad());
        nomad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Simultaneous damage from multiple sources removes only one counter")
    void simultaneousDamageFromMultipleSourcesRemovesOnlyOneCounter() {
        Permanent nomad = addCreatureReady(player1, new PhantomNomad());
        nomad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent firstBlocker = addCreatureReady(player2, new SuntailHawk());
        Permanent secondBlocker = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1));

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isZero();
    }

    private void dealEmberShot(Permanent target) {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new PhantomNomad()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
