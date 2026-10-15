package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nightsnare.class, Forest.class, GrizzlyBears.class, Peek.class})
class NightsnareTest extends BaseCardTest {

    private void castNightsnare() {
        harness.setHand(player1, List.of(new Nightsnare()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Choosing a nonland card makes the opponent discard exactly that card")
    void choosingNonlandDiscardsIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        castNightsnare();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Lands cannot be chosen")
    void landsCannotBeChosen() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        castNightsnare();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Declining the choice makes the opponent discard two cards of their own choice")
    void decliningDiscardsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        castNightsnare();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        // The opponent picks their own discards — lands are allowed here.
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .satisfies(card -> assertThat(card.getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    @DisplayName("A hand of only lands offers no choice and discards two cards instead")
    void onlyLandsFallsBackToDiscardTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        castNightsnare();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty hand resolves with no prompt at all")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());
        castNightsnare();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Nightsnare");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Nightsnare(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Declining with one nonland card discards only that available card")
    void decliningWithOneCardDiscardsIt() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castNightsnare();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Nightsnare");
    }

    @Test
    @DisplayName("A lone land is discarded without requiring a second card")
    void loneLandFallsBackToDiscardAvailableCard() {
        harness.setHand(player2, List.of(new Forest()));
        castNightsnare();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Nightsnare");
    }

    @Test
    @DisplayName("Only the caster chooses a nonland, and only the opponent chooses fallback discards")
    void choicesBelongToTheCorrectPlayers() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek(), new Forest()));
        castNightsnare();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);

        harness.handleCardChosen(player1, -1);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
