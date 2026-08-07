package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurningTreeEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield adds {R}{G} to its controller's mana pool")
    void etbAddsRedAndGreenMana() {
        castEmissary();
        resolveStack();

        harness.assertOnBattlefield(player1, "Burning-Tree Emissary");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent gains no mana from the ETB trigger")
    void opponentGainsNoMana() {
        castEmissary();
        resolveStack();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("The mana added can pay for a second Burning-Tree Emissary")
    void manaPaysForAnotherEmissary() {
        harness.setHand(player1, List.of(new BurningTreeEmissary(), new BurningTreeEmissary()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveStack();

        harness.castCreature(player1, 0);
        resolveStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void castEmissary() {
        harness.setHand(player1, List.of(new BurningTreeEmissary()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
