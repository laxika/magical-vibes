package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Castigate.class, GruulTurf.class, Cremate.class, DouseInGloom.class})
class CastigateTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new Castigate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Reveals the opponent's hand and allows choosing a nonland card")
    void revealsHandAndFiltersNonlandCards() {
        harness.setHand(player2, List.of(new GruulTurf(), new Cremate(), new DouseInGloom()));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.exileMode()).isTrue();
        assertThat(choice.validIndices()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("Exiles the chosen nonland card and leaves lands in hand")
    void exilesChosenNonlandCard() {
        harness.setHand(player2, List.of(new GruulTurf(), new Cremate(), new DouseInGloom()));

        castAndResolve();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getName().equals("Cremate"));
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gruul Turf", "Douse in Gloom");
        harness.assertInGraveyard(player1, "Castigate");
    }

    @Test
    @DisplayName("Does not prompt when the opponent has no nonland cards")
    void doesNotPromptWhenNoNonlandCardsExist() {
        harness.setHand(player2, List.of(new GruulTurf()));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gruul Turf");
        harness.assertInGraveyard(player1, "Castigate");
    }

    @Test
    @DisplayName("Cannot choose a land card")
    void cannotChooseLandCard() {
        harness.setHand(player2, List.of(new GruulTurf(), new Cremate()));

        castAndResolve();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Castigate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
