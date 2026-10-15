package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoolsTome.class, HornedTurtle.class})
class FoolsTomeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and taps when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(tome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot activate while the controller holds a card")
    void cannotActivateWithCardsInHand() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of(new HornedTurtle()));
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(tome.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Still draws if the controller gets a card after activation")
    void conditionIsCheckedWhenActivated() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new HornedTurtle()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(tome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without paying two mana")
    void cannotActivateWithInsufficientMana() {
        Permanent tome = addTome();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(tome.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Tome even with an empty hand")
    void cannotActivateWhenTapped() {
        Permanent tome = addTome();
        tome.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent's hand does not restrict activation or receive the draw")
    void opponentHandDoesNotPreventDrawing() {
        addTome();
        harness.setHand(player1, List.of());
        HornedTurtle drawnCard = new HornedTurtle();
        HornedTurtle opponentCard = new HornedTurtle();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
    }
    private Permanent addTome() {
        return harness.addToBattlefieldAndReturn(player1, new FoolsTome());
    }
}
