package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShatteredDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals an opponent's hand and allows choosing an artifact")
    void promptsForArtifactChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new AccordersShield(), new Peek())));
        castShatteredDreams();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Discards the chosen artifact")
    void discardsChosenArtifact() {
        harness.setHand(player2, new ArrayList<>(List.of(new AccordersShield(), new Peek())));
        castShatteredDreams();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Accorder's Shield");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Peek");
    }

    @Test
    @DisplayName("Non-artifact cards cannot be chosen")
    void nonArtifactCardsAreExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new Peek())));
        castShatteredDreams();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new ShatteredDreams()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShatteredDreams() {
        harness.setHand(player1, List.of(new ShatteredDreams()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
