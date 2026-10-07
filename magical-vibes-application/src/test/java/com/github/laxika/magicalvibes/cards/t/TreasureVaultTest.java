package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreasureVault.class)
class TreasureVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Produces one colorless mana")
    void producesColorlessMana() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new TreasureVault());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself and creates X Treasure tokens")
    void sacrificesItselfAndCreatesTreasures() {
        harness.addToBattlefield(player1, new TreasureVault());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Treasure Vault");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Sacrifice is paid before the Treasure ability resolves")
    void sacrificesAsCostAndUsesTheStack() {
        harness.addToBattlefield(player1, new TreasureVault());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, 2, null);

        harness.assertNotOnBattlefield(player1, "Treasure Vault");
        harness.assertInGraveyard(player1, "Treasure Vault");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("X may be zero, still requiring sacrifice and creating no Treasures")
    void zeroXStillSacrificesVault() {
        harness.addToBattlefield(player1, new TreasureVault());

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Treasure Vault");
        harness.assertNotOnBattlefield(player1, "Treasure Vault");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Both X symbols must be paid before tapping or sacrificing")
    void insufficientManaDoesNotPayOtherCosts() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new TreasureVault());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Treasure Vault");
        harness.assertNotInGraveyard(player1, "Treasure Vault");
        assertThat(vault.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Vault cannot activate its Treasure ability")
    void cannotUseBothTapAbilitiesWithoutUntapping() {
        harness.addToBattlefield(player1, new TreasureVault());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Treasure Vault");
        harness.assertNotInGraveyard(player1, "Treasure Vault");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
