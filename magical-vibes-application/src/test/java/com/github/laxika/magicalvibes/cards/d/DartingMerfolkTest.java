package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DartingMerfolk.class})
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

    @Test
    @DisplayName("Activating a controlled Darting Merfolk returns it to its owner's hand")
    void activateAbilityReturnsToOwnersHandWhenControlledByOpponent() {
        DartingMerfolk merfolk = new DartingMerfolk();
        merfolk.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, merfolk);

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Darting Merfolk");
        harness.assertNotInHand(player2, "Darting Merfolk");
        harness.assertNotOnBattlefield(player2, "Darting Merfolk");
    }

    @Test
    @DisplayName("Tapped and summoning-sick Merfolk can return itself without tapping as a cost")
    void canActivateWhileTappedAndSummoningSick() {
        var merfolk = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        merfolk.setTapped(true);
        merfolk.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertOnBattlefield(player1, "Darting Merfolk");
        harness.assertNotInHand(player1, "Darting Merfolk");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Darting Merfolk");
        harness.assertNotOnBattlefield(player1, "Darting Merfolk");
    }

    @Test
    @DisplayName("Only the Merfolk that activated the ability returns to hand")
    void returnsOnlyTheSourceAmongMultipleCopies() {
        var other = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        var source = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player1.getId())).contains(source.getCard());
    }

    @Test
    @DisplayName("A second activation does nothing once the source has returned to hand")
    void repeatedActivationsReturnTheSourceOnlyOnce() {
        var source = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Darting Merfolk");
        assertThat(gd.playerHands.get(player1.getId())).containsOnlyOnce(source.getCard());
    }
}
