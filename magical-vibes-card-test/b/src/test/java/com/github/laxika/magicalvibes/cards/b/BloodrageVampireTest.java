package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodrageVampire.class, Shock.class})
class BloodrageVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castVampire();

        assertThat(findPermanent(player1, "Bloodrage Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castVampire();

        assertThat(findPermanent(player1, "Bloodrage Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castVampire();

        assertThat(findPermanent(player1, "Bloodrage Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst grants only one counter regardless of damage amount")
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 7);
        castVampire();

        assertThat(findPermanent(player1, "Bloodrage Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A lower opponent life total alone does not enable bloodthirst")
    void lowerLifeTotalDoesNotEnableBloodthirst() {
        harness.setLife(player2, 17);
        castVampire();

        assertThat(findPermanent(player1, "Bloodrage Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst checks damage when the creature enters, not when it is cast")
    void damageWhileSpellIsOnStackEnablesBloodthirst() {
        harness.setHand(player1, List.of(new BloodrageVampire(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bloodrage Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst also applies to entry without casting and creates no trigger")
    void bloodthirstAppliesImmediatelyWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        var vampire = harness.enterBattlefieldAndReturn(player1, new BloodrageVampire());

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castVampire() {
        harness.castFromHand(player1, new BloodrageVampire(), "{2}{B}");
        resolveAllTriggers();
    }
}
