package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelheartVialTest extends BaseCardTest {

    @Test
    @DisplayName("May put the damage dealt as charge counters on Angelheart Vial")
    void damageTriggerAddsDamageAmountAsChargeCounters() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AngelheartVial());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
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
}
