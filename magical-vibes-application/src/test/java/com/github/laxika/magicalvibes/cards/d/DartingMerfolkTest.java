package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DartingMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} ability puts return-to-hand on the stack")
    void activateAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new DartingMerfolk());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating {U} ability returns Darting Merfolk to owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new DartingMerfolk());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Darting Merfolk");
        harness.assertNotOnBattlefield(player1, "Darting Merfolk");
    }
}
