package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlimpseTheCosmosTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest on the bottom in order")
    void choosesOneAndReordersTheRest() {
        Card c0 = new GrizzlyBears();
        Card c1 = new Shock();
        Card c2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(c0, c1, c2));
        harness.setHand(player1, List.of(new GlimpseTheCosmos()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(reveal.allCards()).containsExactly(c0, c1, c2);

        harness.handleMultipleCardsChosen(player1, List.of(c1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(c1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).toBottom())
                .isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c2, c0);
        harness.assertInGraveyard(player1, "Glimpse the Cosmos");
    }

    @Test
    @DisplayName("Can be cast from the graveyard for blue mana while controlling a Giant")
    void castsFromGraveyardWithGiant() {
        harness.setGraveyard(player1, List.of(new GlimpseTheCosmos()));
        harness.addToBattlefield(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isExileInsteadOfGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard without a Giant")
    void cannotCastFromGraveyardWithoutGiant() {
        harness.setGraveyard(player1, List.of(new GlimpseTheCosmos()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("A graveyard cast is exiled after resolving")
    void graveyardCastIsExiledAfterResolving() {
        Card c0 = new GrizzlyBears();
        Card c1 = new Shock();
        Card c2 = new Plains();
        harness.setLibrary(player1, List.of(c0, c1, c2));
        harness.setGraveyard(player1, List.of(new GlimpseTheCosmos()));
        harness.addToBattlefield(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(c0.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        harness.assertNotInGraveyard(player1, "Glimpse the Cosmos");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Glimpse the Cosmos"));
    }
}
