package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

    private Permanent addCryptRats(Player player) {
        return addCreatureReady(player, new CryptRats());
    }
}
