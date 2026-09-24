package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArcaneTeachings;
import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.l.LightningSurge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneTeachings.class, BorderPatrol.class, EmberShot.class, FlaringPain.class, LavaDart.class, LightningSurge.class, PhantomTiger.class})
class PhantomTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new PhantomTiger()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent tiger = findPermanent(player1, "Phantom Tiger");
        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents Ember Shot damage and removes one +1/+1 counter")
    void preventsEmberShotDamageAndRemovesOneCounter() {
        Permanent tiger = harness.enterBattlefieldAndReturn(player2, new PhantomTiger());

        harness.setLibrary(player1, List.of(new PhantomTiger()));
        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castInstant(player1, 0, tiger.getId());
        harness.passBothPriorities();

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tiger.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounter() {
        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new PhantomTiger());

        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new BorderPatrol());
        attacker.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Simultaneous damage from multiple blockers removes only one +1/+1 counter")
    void simultaneousDamageFromMultipleBlockersRemovesOneCounter() {
        Permanent tiger = harness.enterBattlefieldAndReturn(player1, new PhantomTiger());
        tiger.setSummoningSick(false);

        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new BorderPatrol());
        harness.enterBattlefieldAndReturn(player2, new BorderPatrol());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tiger.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Continues preventing damage with no +1/+1 counters")
    void preventsDamageWithoutCounters() {
        Permanent tiger = harness.enterBattlefieldAndReturn(player2, new PhantomTiger());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, tiger.getId());
        harness.passBothPriorities();

        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.setLibrary(player1, List.of(new PhantomTiger()));
        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveInstant(player1, 0, tiger.getId());

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(tiger.getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Phantom Tiger")).isSameAs(tiger);
    }

    @Test
    @DisplayName("Removes a counter even when damage cannot be prevented")
    void removesCounterWhenDamageCannotBePrevented() {
        Permanent tiger = harness.enterBattlefieldAndReturn(player2, new PhantomTiger());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, tiger.getId());
        harness.passBothPriorities();

        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setLibrary(player1, List.of(new PhantomTiger()));
        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveInstant(player1, 0, tiger.getId());

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(tiger.getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanent(player2, "Phantom Tiger")).isSameAs(tiger);
    }

    @Test
    @DisplayName("Prevents noncombat damage and removes one +1/+1 counter")
    void preventsNoncombatDamageAndRemovesOneCounter() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, tiger.getId());
        harness.passBothPriorities();

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tiger.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tiger);
    }

    @Test
    @DisplayName("Removing the last +1/+1 counter makes the 1/0 creature die")
    void diesWhenLastCounterIsRemoved() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castLavaDartForJudReview(tiger);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tiger);
    }

    @Test
    @DisplayName("Unpreventable damage still removes one +1/+1 counter")
    void unpreventableDamageStillRemovesOneCounter() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        castLavaDartForJudReview(tiger);

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(tiger.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tiger);
    }

    private void castLavaDartForJudReview(Permanent target) {
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
