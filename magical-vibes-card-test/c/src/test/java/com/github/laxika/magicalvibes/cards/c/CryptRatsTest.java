package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptRats.class, GrizzlyBears.class})
class CryptRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each creature and each player")
    void dealsDamageToEachCreatureAndPlayer() {
        addCryptRats(player1);
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears"); // 2/2 dies to 3
        harness.assertInGraveyard(player1, "Crypt Rats"); // its own 1/1 dies to the blast
    }

    @Test
    @DisplayName("Cannot spend nonblack mana on X")
    void cannotSpendNonblackManaOnX() {
        addCryptRats(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("X=0 deals no damage and kills nothing")
    void zeroDamageDoesNothing() {
        addCryptRats(player1);
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Crypt Rats");
    }

    private void addCryptRats(Player player) {
        addCreatureReady(player, new CryptRats());
    }

    @Test
    @CardUsed({CryptRats.class})
    void canActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CryptRats());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Crypt Rats");
    }

    @Test
    void stackedActivationsResolveAfterSourceDiesWithTheirOwnXValues() {
        addCryptRats(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 2, null);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Crypt Rats");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({CryptRats.class})
    void otherManaCannotSupplementInsufficientBlackMana() {
        addCryptRats(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crypt Rats");
    }
}
