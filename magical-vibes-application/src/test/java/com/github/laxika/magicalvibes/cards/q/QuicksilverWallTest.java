package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicksilverWallTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {4} returns Quicksilver Wall to its owner's hand")
    void controllerCanReturnWallToHand() {
        harness.addToBattlefield(player1, new QuicksilverWall());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Quicksilver Wall");
        harness.assertNotOnBattlefield(player1, "Quicksilver Wall");
    }

    @Test
    @DisplayName("Any player may pay {4} to return Quicksilver Wall to its owner's hand")
    void opponentCanReturnWallToOwnersHand() {
        harness.addToBattlefield(player1, new QuicksilverWall());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Quicksilver Wall");
        harness.assertNotInHand(player2, "Quicksilver Wall");
        harness.assertNotOnBattlefield(player1, "Quicksilver Wall");
    }

    @Test
    @DisplayName("Quicksilver Wall's ability requires four mana")
    void cannotActivateWithoutFourMana() {
        harness.addToBattlefield(player1, new QuicksilverWall());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
