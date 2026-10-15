package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.ReflectingPool;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FulminatorMage.class, ReflectingPool.class, Forest.class})
class FulminatorMageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Fulminator Mage and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new FulminatorMage());
        harness.addToBattlefield(player2, new ReflectingPool());
        UUID targetId = harness.getPermanentId(player2, "Reflecting Pool");

        harness.activateAbility(player1, 0, 0, null, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Fulminator Mage");
        harness.assertInGraveyard(player1, "Fulminator Mage");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving destroys the target nonbasic land")
    void resolvingDestroysNonbasicLand() {
        harness.addToBattlefield(player1, new FulminatorMage());
        harness.addToBattlefield(player2, new ReflectingPool());
        UUID targetId = harness.getPermanentId(player2, "Reflecting Pool");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Reflecting Pool");
        harness.assertInGraveyard(player2, "Reflecting Pool");
    }

    @Test
    @DisplayName("Can target own nonbasic land")
    void canTargetOwnNonbasicLand() {
        harness.addToBattlefield(player1, new FulminatorMage());
        harness.addToBattlefield(player1, new ReflectingPool());
        UUID targetId = harness.getPermanentId(player1, "Reflecting Pool");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reflecting Pool");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new FulminatorMage());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland and does not sacrifice on rejected activation")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new FulminatorMage());
        harness.addToBattlefield(player2, new FulminatorMage());
        UUID targetId = harness.getPermanentId(player2, "Fulminator Mage");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Fulminator Mage");
        harness.assertNotInGraveyard(player1, "Fulminator Mage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice a tapped Mage with summoning sickness")
    void canActivateWhileTappedAndSummoningSick() {
        var mage = harness.addToBattlefieldAndReturn(player1, new FulminatorMage());
        mage.tap();
        mage.setSummoningSick(true);
        harness.addToBattlefield(player2, new ReflectingPool());
        UUID targetId = harness.getPermanentId(player2, "Reflecting Pool");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fulminator Mage");
        harness.assertInGraveyard(player2, "Reflecting Pool");
    }

    @Test
    @DisplayName("Target leaving before resolution does not refund the sacrifice")
    void targetLeavingDoesNotRefundSacrifice() {
        harness.addToBattlefield(player1, new FulminatorMage());
        harness.addToBattlefield(player2, new FulminatorMage());
        harness.addToBattlefield(player2, new ReflectingPool());
        UUID targetId = harness.getPermanentId(player2, "Reflecting Pool");

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.activateAbility(player2, 0, 0, null, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fulminator Mage");
        harness.assertInGraveyard(player2, "Fulminator Mage");
        harness.assertInGraveyard(player2, "Reflecting Pool");
        assertThat(gd.stack).isEmpty();
    }
}
