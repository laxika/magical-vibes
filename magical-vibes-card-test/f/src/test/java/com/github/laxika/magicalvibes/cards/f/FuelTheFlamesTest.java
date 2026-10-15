package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuelTheFlames.class, FugitiveWizard.class, AirElemental.class})
class FuelTheFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature but not to players")
    void dealsDamageToEachCreatureOnly() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new FuelTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        assertThat(airElemental.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling {2} discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new FuelTheFlames()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fuel the Flames");
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cycling discards as a cost, draws on resolution, and deals no damage")
    void cyclingDiscardsBeforeDrawingWithoutDealingDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new FuelTheFlames()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Fuel the Flames");
        harness.assertNotInHand(player1, "Fuel the Flames");
        harness.assertNotInHand(player1, "Air Elemental");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new FuelTheFlames()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fuel the Flames");
        harness.assertNotInGraveyard(player1, "Fuel the Flames");
        harness.assertNotInHand(player1, "Air Elemental");
        assertThat(gd.stack).isEmpty();
    }
}
