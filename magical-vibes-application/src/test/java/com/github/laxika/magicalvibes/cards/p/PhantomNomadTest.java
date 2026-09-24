package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cagemail;
import com.github.laxika.magicalvibes.cards.d.DwarvenDriller;
import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
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

@CardUsed({Cagemail.class, DwarvenDriller.class, EmberShot.class, FlaringPain.class, LavaDart.class, PhantomNomad.class, SuntailHawk.class})
class PhantomNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new PhantomNomad()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent nomad = findNomad(player1);
        assertThat(nomad).isNotNull();
        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents Lava Dart damage and removes one +1/+1 counter")
    void preventsLavaDartDamageAndRemovesOneCounter() {
        Permanent nomad = harness.enterBattlefieldAndReturn(player2, new PhantomNomad());

        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nomad.getId());

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Continues preventing damage after its last counter is removed")
    void preventsDamageWithoutCounters() {
        Permanent nomad = harness.enterBattlefieldAndReturn(player2, new PhantomNomad());

        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, nomad.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LavaDart(), new LavaDart(), new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, nomad.getId());
        harness.castAndResolveInstant(player1, 0, nomad.getId());
        harness.castAndResolveInstant(player1, 0, nomad.getId());

        assertThat(findNomad(player2)).isSameAs(nomad);
        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nomad.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removes a counter even when damage cannot be prevented")
    void removesCounterWhenDamageCannotBePrevented() {
        Permanent nomad = harness.enterBattlefieldAndReturn(player2, new PhantomNomad());

        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, nomad.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nomad.getId());

        assertThat(findNomad(player2)).isSameAs(nomad);
        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounter() {
        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new PhantomNomad());

        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new DwarvenDriller());
        attacker.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removes only one counter for damage from multiple simultaneous blockers")
    void removesOneCounterForMultipleSimultaneousSources() {
        Permanent nomad = harness.enterBattlefieldAndReturn(player1, new PhantomNomad());
        nomad.setSummoningSick(false);
        Permanent blocker1 = harness.enterBattlefieldAndReturn(player2, new DwarvenDriller());
        harness.enterBattlefieldAndReturn(player2, new DwarvenDriller());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker1.getId(), 2));

        assertThat(findNomad(player1)).isSameAs(nomad);
        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isZero();
    }

    private Permanent findNomad(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PhantomNomad)
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("Prevents direct damage and removes one +1/+1 counter")
    void preventsDirectDamageAndRemovesOneCounter() {
        Permanent nomad = addCreatureReady(player2, new PhantomNomad());
        nomad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        dealEmberShotForJudReview(nomad);

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Unpreventable damage still removes one +1/+1 counter")
    void unpreventableDamageStillRemovesOneCounter() {
        Permanent nomad = addCreatureReady(player2, new PhantomNomad());
        nomad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        nomad.setToughnessModifier(3);

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        dealEmberShotForJudReview(nomad);

        assertThat(nomad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nomad.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nomad);
    }

    @Test
    @DisplayName("Prevents combat damage and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounterJudReview() {
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

    private void dealEmberShotForJudReview(Permanent target) {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new PhantomNomad()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
