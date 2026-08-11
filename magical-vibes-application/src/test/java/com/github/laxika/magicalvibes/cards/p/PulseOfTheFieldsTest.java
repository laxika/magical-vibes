package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PulseOfTheFieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life and returns to hand when an opponent still has more life")
    void returnsToHandWhenOpponentStillHasMoreLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(handNames(player1)).containsExactly("Pulse of the Fields");
        assertThat(graveyardNames(player1)).doesNotContain("Pulse of the Fields");
    }

    @Test
    @DisplayName("Gains 4 life and goes to the graveyard when no opponent has more life afterward")
    void goesToGraveyardWhenOpponentDoesNotHaveMoreLifeAfterward() {
        harness.setLife(player1, 18);
        harness.setLife(player2, 20);

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(handNames(player1)).doesNotContain("Pulse of the Fields");
        assertThat(graveyardNames(player1)).containsExactly("Pulse of the Fields");
    }

    private void cast() {
        harness.setHand(player1, List.of(new PulseOfTheFields()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(card -> card.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(card -> card.getName()).toList();
    }
}
