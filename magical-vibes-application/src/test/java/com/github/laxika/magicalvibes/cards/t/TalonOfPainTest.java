package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalonOfPain.class, Fireball.class, Juggernaut.class})
class TalonOfPainTest extends BaseCardTest {

    @Test
    @DisplayName("Damage from another source puts a charge counter on Talon of Pain")
    void anotherSourceDamageAddsChargeCounter() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 2, player2.getId());
        resolveAllTriggers();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage from an opponent's source does not put a charge counter on Talon of Pain")
    void opponentSourceDamageDoesNotAddChargeCounter() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castSorcery(player2, 0, 2, player1.getId());
        resolveAllTriggers();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage to a creature does not trigger Talon of Pain's charge-counter ability")
    void damageToCreatureDoesNotAddChargeCounter() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, juggernaut.getId());

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(juggernaut.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Talon of Pain's own damage does not trigger its charge-counter ability")
    void ownDamageDoesNotAddChargeCounter() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        talon.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(talon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing X charge counters deals X damage to any target")
    void removesXChargeCountersAndDealsDamage() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        talon.setCounterCount(CounterType.CHARGE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The activated ability can deal X damage to a creature")
    void activatedAbilityCanDamageCreature() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        talon.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, juggernaut.getId());
        harness.passBothPriorities();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(juggernaut.getMarkedDamage()).isEqualTo(2);
        assertThat(talon.isTapped()).isTrue();
    }
}
