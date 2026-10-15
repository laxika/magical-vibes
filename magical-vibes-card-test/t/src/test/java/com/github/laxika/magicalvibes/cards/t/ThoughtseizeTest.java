package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thoughtseize.class, Forest.class, GrizzlyBears.class, Peek.class})
class ThoughtseizeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Thoughtseize()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Choosing a nonland card discards it and caster loses 2 life")
    void choosingNonlandDiscardsAndLosesLife() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Reveal + choose interaction pauses resolution
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);

        harness.handleCardChosen(player1, 0);

        // Choice complete, chosen card discarded
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Caster lost 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcludedFromChoices() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Caster still loses 2 life when target hand is empty")
    void losesLifeWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // No interaction — resolution runs through the life loss
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Caster still loses 2 life when target hand has only lands")
    void losesLifeWithOnlyLands() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target yourself and discard a nonland card from your own hand")
    void canTargetYourself() {
        harness.setHand(player1, List.of(new Thoughtseize(), new Thoughtseize(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Thoughtseize")).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Caster must choose a nonland card and the target cannot make the choice")
    void choiceIsMandatoryAndBelongsToCaster() {
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.setHand(player2, List.of(new Forest(), new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Thoughtseize");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can choose an instant and only the caster loses life")
    void canChooseInstant() {
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Thoughtseize");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
