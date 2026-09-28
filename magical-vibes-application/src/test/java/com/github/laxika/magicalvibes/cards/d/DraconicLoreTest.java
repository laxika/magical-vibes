package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdultGoldDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconicLore.class, AdultGoldDragon.class, GrizzlyBears.class})
class DraconicLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards")
    void drawsThreeCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DraconicLore()));
        addMana(6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires the full cost without a Dragon")
    void requiresFullCostWithoutDragon() {
        harness.setHand(player1, List.of(new DraconicLore()));
        addMana(4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs two less when a Dragon is controlled")
    void costsTwoLessWithDragon() {
        harness.addToBattlefield(player1, new AdultGoldDragon());
        harness.setHand(player1, List.of(new DraconicLore()));
        addMana(4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addMana(int total) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, total - 1);
    }
}
