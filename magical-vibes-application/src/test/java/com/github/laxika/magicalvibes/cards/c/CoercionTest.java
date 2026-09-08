package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvenCache;
import com.github.laxika.magicalvibes.cards.e.Everglades;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Coercion.class, ElvenCache.class, Impulse.class, Everglades.class})
class CoercionTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a card from opponent's hand and it is discarded")
    void choosingCardDiscardsIt() {
        harness.setHand(player2, List.of(new ElvenCache(), new Impulse()));

        harness.setHand(player1, List.of(new Coercion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId())
                .isEqualTo(player1.getId());
        assertThat(choice.prompt()).isEqualTo("Choose a card to discard.");
        assertThat(gameLogContains("reveals their hand")).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Elven Cache");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Impulse");
    }

    @Test
    @DisplayName("Any card type is a valid choice, including lands")
    void landsAreValidChoices() {
        harness.setHand(player2, List.of(new ElvenCache(), new Everglades()));

        harness.setHand(player1, List.of(new Coercion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Everglades");
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Coercion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Coercion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Coercion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
