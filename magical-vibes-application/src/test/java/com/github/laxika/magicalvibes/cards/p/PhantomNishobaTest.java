package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArcaneTeachings;
import com.github.laxika.magicalvibes.cards.c.Cagemail;
import com.github.laxika.magicalvibes.cards.d.DwarvenDriller;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.s.Swelter;
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

@CardUsed({PhantomNishoba.class, ArcaneTeachings.class, Cagemail.class, DwarvenDriller.class,
        FlaringPain.class, LavaDart.class, Swelter.class})
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
        Permanent nishoba = harness.enterBattlefieldAndReturn(player2, new PhantomNishoba());
        Permanent otherTarget = addCreatureReady(player2, new DwarvenDriller());

        harness.setHand(player1, List.of(new Swelter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, List.of(nishoba.getId(), otherTarget.getId()));

        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nishoba.getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Phantom Nishoba")).isSameAs(nishoba);
    }

    @Test
    @DisplayName("Gains life equal to damage dealt in combat")
    void gainsLifeEqualToDamageDealt() {
        Permanent nishoba = addAttacker(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(27);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Still prevents damage when it has no +1/+1 counters")
    void preventsDamageWithoutCounters() {
        Permanent nishoba = harness.enterBattlefieldAndReturn(player2, new PhantomNishoba());

        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, nishoba.getId());
        harness.passBothPriorities();

        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nishoba.getId());

        assertThat(findPermanent(player2, "Phantom Nishoba")).isSameAs(nishoba);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nishoba.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removes one counter even when damage cannot be prevented")
    void removesOneCounterWhenDamageCannotBePrevented() {
        Permanent nishoba = harness.enterBattlefieldAndReturn(player2, new PhantomNishoba());

        harness.setHand(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nishoba.getId());

        assertThat(findPermanent(player2, "Phantom Nishoba")).isSameAs(nishoba);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nishoba.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes only one counter for damage from multiple simultaneous sources")
    void removesOneCounterForMultipleSimultaneousSources() {
        Permanent nishoba = harness.enterBattlefieldAndReturn(player1, new PhantomNishoba());
        nishoba.setSummoningSick(false);
        Permanent blocker1 = harness.enterBattlefieldAndReturn(player2, new DwarvenDriller());
        Permanent blocker2 = harness.enterBattlefieldAndReturn(player2, new DwarvenDriller());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 2,
                blocker2.getId(), 5));

        assertThat(findPermanent(player1, "Phantom Nishoba")).isSameAs(nishoba);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(nishoba.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage also grants that much life")
    void noncombatDamageAlsoGrantsThatMuchLife() {
        Permanent nishoba = harness.enterBattlefieldAndReturn(player1, new PhantomNishoba());
        nishoba.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, nishoba.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent nishoba = harness.enterBattlefieldAndReturn(player, new PhantomNishoba());
        nishoba.setSummoningSick(false);
        nishoba.setAttacking(true);
        return nishoba;
    }
}
