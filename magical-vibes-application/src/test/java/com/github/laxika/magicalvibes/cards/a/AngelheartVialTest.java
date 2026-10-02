package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AngelheartVial.class, Shock.class, GrizzlyBears.class})
class AngelheartVialTest extends BaseCardTest {

    @Test
    @DisplayName("May put the damage dealt as charge counters on Angelheart Vial")
    void damageTriggerAddsDamageAmountAsChargeCounters() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the damage trigger does not add counters")
    void decliningDamageTriggerAddsNoCounters() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing four counters gains life and draws a card")
    void removesCountersGainsLifeAndDraws() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        vial.setCounterCount(CounterType.CHARGE, 4);
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(vial.isTapped()).isTrue();
    }

    @Test
    void damageToOpponentDoesNotTriggerVial() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(vial.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void simultaneousCombatDamageOffersOneChoiceForTotalDamage() {
        Permanent vial = harness.addToBattlefieldAndReturn(player2, new AngelheartVial());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).setSummoningSick(false);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void activationPaysExactlyFourCountersBeforeResolving() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        vial.setCounterCount(CounterType.CHARGE, 6);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(vial.isTapped()).isTrue();
        harness.assertLife(player1, 10);
        harness.assertNotInHand(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void cannotActivateWithOnlyThreeChargeCounters() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        vial.setCounterCount(CounterType.CHARGE, 3);
        vial.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(vial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vial.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        vial.setCounterCount(CounterType.CHARGE, 4);
        vial.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
