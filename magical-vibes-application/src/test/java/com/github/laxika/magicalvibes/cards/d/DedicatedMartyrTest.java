package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DedicatedMartyrTest extends BaseCardTest {

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
}
