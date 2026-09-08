package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
