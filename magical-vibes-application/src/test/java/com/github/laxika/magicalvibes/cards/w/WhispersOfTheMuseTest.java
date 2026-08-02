package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhispersOfTheMuseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving without buyback draws a card and goes to the graveyard")
    void drawsWithoutBuyback() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WhispersOfTheMuse()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(handNames(player1)).containsExactly("Grizzly Bears");
        assertThat(graveyardNames(player1)).containsExactly("Whispers of the Muse");
    }

    @Test
    @DisplayName("Paying buyback draws a card and returns the spell to hand")
    void buybackReturnsToHand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WhispersOfTheMuse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithBuyback(player1, 0, null);
        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(handNames(player1)).containsExactlyInAnyOrder("Grizzly Bears", "Whispers of the Muse");
        assertThat(graveyardNames(player1)).isEmpty();
    }

    @Test
    @DisplayName("Paying buyback with insufficient mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WhispersOfTheMuse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Whispers of the Muse");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getName).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(Card::getName).toList();
    }
}
