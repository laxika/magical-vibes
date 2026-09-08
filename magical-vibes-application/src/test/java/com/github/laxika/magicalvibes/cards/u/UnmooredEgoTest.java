package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

class UnmooredEgoTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only nonartifact, nonland card names and targets an opponent")
    void offersOnlyAllowedCardNames() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Ornithopter(), new Forest())));
        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Grizzly Bears");
        assertThat(choice.options()).doesNotContain("Ornithopter", "Forest");
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Exiles up to four matching cards and draws for matching cards exiled from hand")
    void capsExileSelectionAtFourAndDrawsForHandCopies() {
        Card handBears1 = new GrizzlyBears();
        Card handBears2 = new GrizzlyBears();
        Card graveyardBears1 = new GrizzlyBears();
        Card graveyardBears2 = new GrizzlyBears();
        Card graveyardBears3 = new GrizzlyBears();
        Card graveyardBears4 = new GrizzlyBears();
        Card handPeek = new Peek();
        Card libraryPeek1 = new Peek();
        Card libraryPeek2 = new Peek();

        harness.setHand(player2, new ArrayList<>(List.of(handBears1, handBears2, handPeek)));
        harness.setGraveyard(player2,
                List.of(graveyardBears1, graveyardBears2, graveyardBears3, graveyardBears4));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(libraryPeek1, libraryPeek2));

        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        List<java.util.UUID> fiveCards = List.of(
                handBears1.getId(), handBears2.getId(), graveyardBears1.getId(),
                graveyardBears2.getId(), graveyardBears3.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, fiveCards))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Choose at most 4 cards");

        harness.handleMultipleCardsChosen(player1, List.of(
                handBears1.getId(), handBears2.getId(), graveyardBears1.getId(), graveyardBears2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(
                handBears1, handBears2, graveyardBears1, graveyardBears2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardBears3, graveyardBears4);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Grizzly Bears")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Peek")).hasSize(3);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
