package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconicLore.class, ShivanDragon.class})
class DraconicLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards")
    void drawsThreeCards() {
        Card first = new ShivanDragon();
        Card second = new ShivanDragon();
        Card third = new ShivanDragon();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DraconicLore()));
        addMana(5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Costs {3}{U} when controlling a Dragon")
    void costsLessWhenControllingDragon() {
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.setHand(player1, List.of(new DraconicLore()));
        addMana(3);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a Dragon")
    void requiresFullCostWithoutDragon() {
        harness.setHand(player1, List.of(new DraconicLore()));
        addMana(3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addMana(int genericAmount) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericAmount);
    }
}
