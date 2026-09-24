package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunDroplet.class, ShrapnelBlast.class, Ornithopter.class})
class SunDropletTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the controller adds that many charge counters")
    void damageAddsChargeCounters() {
        Permanent droplet = addSunDroplet();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ShrapnelBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player2, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(droplet.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Damage to another player does not add charge counters")
    void damageToAnotherPlayerDoesNotAddChargeCounters() {
        Permanent droplet = addSunDroplet();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(droplet.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Upkeep: removing a charge counter gains 1 life")
    void upkeepRemoveCounterGainsLife() {
        Permanent droplet = addSunDroplet();
        droplet.setCounterCount(CounterType.CHARGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(droplet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Upkeep: declining keeps the charge counter and gains no life")
    void upkeepDeclineKeepsCounter() {
        Permanent droplet = addSunDroplet();
        droplet.setCounterCount(CounterType.CHARGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(droplet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep: accepting with no charge counters gains no life")
    void upkeepNoCountersGainsNoLife() {
        Permanent droplet = addSunDroplet();
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(droplet.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each upkeep lets the controller remove a charge counter")
    void triggersDuringOpponentsUpkeep() {
        Permanent droplet = addSunDroplet();
        droplet.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(droplet.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent addSunDroplet() {
        return harness.addToBattlefieldAndReturn(player1, new SunDroplet());
    }
}
