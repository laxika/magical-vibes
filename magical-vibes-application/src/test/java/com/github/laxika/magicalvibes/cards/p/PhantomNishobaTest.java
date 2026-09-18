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

@CardUsed({PhantomNishoba.class, EmberShot.class, FlaringPain.class, SuntailHawk.class})
class PhantomNishobaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven +1/+1 counters")
    void entersWithSevenCounters() {
        harness.castFromHand(player1, new PhantomNishoba(), "{5}{G}{W}");
        harness.passBothPriorities();

        Permanent nishoba = findPermanent(player1, "Phantom Nishoba");
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Damage to it is prevented and removes one counter per damage event")
    void damageIsPreventedAndRemovesOneCounterPerDamageEvent() {
        harness.addToBattlefield(player2, new PhantomNishoba());
        Permanent nishoba = findPermanent(player2, "Phantom Nishoba");
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        dealEmberShot(nishoba);

        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nishoba.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage is still prevented when it has no +1/+1 counters")
    void preventsDamageWithoutCounters() {
        Permanent nishoba = addCreatureReady(player2, new PhantomNishoba());
        nishoba.setToughnessModifier(1);

        dealEmberShot(nishoba);

        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nishoba.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nishoba);
    }

    @Test
    @DisplayName("Unpreventable damage still removes one counter per damage event")
    void unpreventableDamageStillRemovesOneCounterPerDamageEvent() {
        Permanent nishoba = addCreatureReady(player2, new PhantomNishoba());
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        dealEmberShot(nishoba);

        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nishoba.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Simultaneous damage from multiple sources removes only one counter")
    void simultaneousDamageFromMultipleSourcesRemovesOnlyOneCounter() {
        Permanent nishoba = addCreatureReady(player1, new PhantomNishoba());
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        Permanent firstBlocker = addCreatureReady(player2, new SuntailHawk());
        Permanent secondBlocker = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 5
        ));

        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nishoba.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gains life equal to damage dealt in combat")
    void gainsLifeEqualToDamageDealt() {
        Permanent nishoba = addAttacker(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 27);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    private void dealEmberShot(Permanent target) {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new PhantomNishoba()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent nishoba = addCreatureReady(player, new PhantomNishoba());
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        nishoba.setAttacking(true);
        return nishoba;
    }

}
