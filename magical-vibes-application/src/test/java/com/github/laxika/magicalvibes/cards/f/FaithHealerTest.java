package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicRenewal;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaithHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an enchantment gains life equal to its mana value")
    void sacrificeEnchantmentGainsItsManaValue() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.addToBattlefield(player1, new AngelicRenewal());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Angelic Renewal");
        harness.assertInGraveyard(player1, "Angelic Renewal");
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Cannot activate without an enchantment to sacrifice")
    void requiresEnchantmentToSacrifice() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
    }
}
