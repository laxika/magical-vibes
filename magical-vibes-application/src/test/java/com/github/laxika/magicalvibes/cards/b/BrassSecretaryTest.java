package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrassSecretaryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Brass Secretary sacrifices it and draws a card")
    void activateAbilitySacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new BrassSecretary());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Brass Secretary");
        harness.assertInGraveyard(player1, "Brass Secretary");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Brass Secretary cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BrassSecretary());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Brass Secretary");
    }
}
