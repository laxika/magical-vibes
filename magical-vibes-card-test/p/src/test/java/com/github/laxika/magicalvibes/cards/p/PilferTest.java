package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Pilfer.class, Forest.class, GrizzlyBears.class})
class PilferTest extends BaseCardTest {

    @Test
    @DisplayName("Targeting yourself is rejected")
    void targetingYourselfIsRejected() {
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolves by revealing the opponent's hand and prompting for a nonland card")
    void promptsForNonlandCard() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(land, creature)));
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a nonland card discards it")
    void choosingNonlandCardDiscardsIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Lands cannot be chosen")
    void landsCannotBeChosen() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty hand resolves without a choice or discard")
    void emptyHandResolvesWithoutChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Pilfer");
        harness.assertNotInGraveyard(player2, "Pilfer");
    }

    @Test
    @DisplayName("The caster must choose a nonland card and cannot decline")
    void rejectsLandAndDecliningWhenNonlandAvailable() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, List.of(land, creature));
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A noncreature spell can be chosen and only one card is discarded")
    void choosesExactlyOneOfMultipleNonlands() {
        Card spell = new Pilfer();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, List.of(spell, creature, land));
        harness.setHand(player1, List.of(new Pilfer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Pilfer");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
