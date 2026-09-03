package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeweledAmulet.class, BalduvianBears.class})
class JeweledAmuletTest extends BaseCardTest {

    /** Activates the first ability paying {1} with a single mana of the given color. */
    private void chargeWith(ManaColor color) {
        harness.addMana(player1, color, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("First ability puts a charge counter on Jeweled Amulet and notes the mana spent")
    void firstAbilityChargesAndNotes() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());
        chargeWith(ManaColor.GREEN);

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(harness.getGameData().notedMana.get(amulet.getCard().getId()))
                .containsEntry(ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("First ability can't be activated while a charge counter is on Jeweled Amulet")
    void firstAbilityBlockedByChargeCounter() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());
        chargeWith(ManaColor.GREEN);
        amulet.untap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability removes the charge counter and adds one mana of the noted type")
    void secondAbilityAddsNotedMana() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());
        chargeWith(ManaColor.GREEN);
        amulet.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isZero();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getTotal()).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noted mana is unrestricted and can pay for any spell")
    void notedManaIsUnrestricted() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());
        chargeWith(ManaColor.GREEN);
        amulet.untap();
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player1, List.of(new BalduvianBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Balduvian Bears")).isNotNull();
    }

    @Test
    @DisplayName("Colorless mana spent on the first ability is noted as colorless")
    void colorlessManaIsNoted() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());
        chargeWith(ManaColor.COLORLESS);
        amulet.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = harness.getGameData().playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can't be activated without a charge counter")
    void secondAbilityNeedsChargeCounter() {
        harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A charge counter added without noting mana produces no mana")
    void externallyAddedChargeCounterProducesNoManaWithoutNote() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());
        amulet.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Pending charge activations retain the mana spent on each activation")
    void pendingActivationsRetainTheirManaNotes() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new JeweledAmulet());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        amulet.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        resolveAllTriggers();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(harness.getGameData().notedMana.get(amulet.getCard().getId()))
                .containsEntry(ManaColor.GREEN, 1);
    }
}
