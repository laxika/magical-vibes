package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatewatchBeacon.class, JaceWielderOfMysteries.class})
class GatewatchBeaconTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three loyalty counters")
    void entersWithLoyaltyCounters() {
        Permanent beacon = harness.enterBattlefieldAndReturn(player1, new GatewatchBeacon());

        assertThat(beacon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping adds one white mana")
    void tapsForWhiteMana() {
        Permanent beacon = harness.addToBattlefieldAndReturn(player1, new GatewatchBeacon());
        beacon.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(beacon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May move a loyalty counter onto a planeswalker you control")
    void movesLoyaltyCounterOntoControlledPlaneswalker() {
        Permanent beacon = harness.enterBattlefieldAndReturn(player1, new GatewatchBeacon());
        Permanent jace = harness.enterBattlefieldAndReturn(player1, new JaceWielderOfMysteries());
        jace.setCounterCount(CounterType.LOYALTY, 2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(beacon.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's planeswalker or without a loyalty counter")
    void onlyTriggersForControlledPlaneswalkersWhileBeaconHasCounters() {
        Permanent beacon = harness.enterBattlefieldAndReturn(player1, new GatewatchBeacon());
        harness.enterBattlefieldAndReturn(player2, new JaceWielderOfMysteries());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        beacon.setCounterCount(CounterType.LOYALTY, 0);
        harness.enterBattlefieldAndReturn(player1, new JaceWielderOfMysteries());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
