package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(QuicksilverWall.class)
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
    @DisplayName("A controlled Quicksilver Wall returns to its owner's hand")
    void controlledWallReturnsToItsOwnersHand() {
        QuicksilverWall wall = new QuicksilverWall();
        wall.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, wall);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Quicksilver Wall");
        harness.assertNotInHand(player2, "Quicksilver Wall");
        harness.assertNotOnBattlefield(player2, "Quicksilver Wall");
    }

    @Test
    @DisplayName("Quicksilver Wall's ability requires four mana")
    void cannotActivateWithoutFourMana() {
        harness.addToBattlefield(player1, new QuicksilverWall());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wall can activate its ability")
    void tappedSummoningSickWallCanReturnToHand() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new QuicksilverWall());
        wall.tap();
        wall.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.assertOnBattlefield(player1, "Quicksilver Wall");
        harness.assertNotInHand(player1, "Quicksilver Wall");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Quicksilver Wall");
        harness.assertNotOnBattlefield(player1, "Quicksilver Wall");
    }

    @Test
    @DisplayName("An opponent must pay with their own mana")
    void opponentCannotSpendControllersMana() {
        harness.addToBattlefield(player1, new QuicksilverWall());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Quicksilver Wall");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations return only their source, once")
    void repeatedActivationsDoNotReturnAnotherWall() {
        harness.addToBattlefield(player1, new QuicksilverWall());
        Permanent otherWall = harness.addToBattlefieldAndReturn(player1, new QuicksilverWall());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherWall);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Quicksilver Wall");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
