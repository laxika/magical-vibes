package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DedicatedMartyr.class)
class DedicatedMartyrTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice is paid immediately, but life is gained only on resolution")
    void sacrificesAsCostBeforeGainingLife() {
        harness.addToBattlefield(player1, new DedicatedMartyr());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Dedicated Martyr");
        harness.assertInGraveyard(player1, "Dedicated Martyr");
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        var martyr = harness.addToBattlefieldAndReturn(player1, new DedicatedMartyr());
        martyr.tap();
        martyr.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Dedicated Martyr");
    }

    @Test
    @DisplayName("Only the activating controller gains life on the opponent's turn")
    void otherControllerGainsLifeAtInstantSpeed() {
        harness.addToBattlefield(player2, new DedicatedMartyr());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 13);
        harness.assertNotOnBattlefield(player2, "Dedicated Martyr");
        harness.assertInGraveyard(player2, "Dedicated Martyr");
    }

    @Test
    @DisplayName("Pays {W}, sacrifices itself, and gains 3 life")
    void sacrificesItselfToGainLife() {
        harness.addToBattlefield(player1, new DedicatedMartyr());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Dedicated Martyr");
        harness.assertInGraveyard(player1, "Dedicated Martyr");
    }

    @Test
    @DisplayName("Cannot activate without {W}")
    void requiresWhiteMana() {
        harness.addToBattlefield(player1, new DedicatedMartyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the white activation cost with colorless mana")
    void cannotPayWhiteCostWithColorlessMana() {
        harness.addToBattlefield(player1, new DedicatedMartyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dedicated Martyr");
    }
}
