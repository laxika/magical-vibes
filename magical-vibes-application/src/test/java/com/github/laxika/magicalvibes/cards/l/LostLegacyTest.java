package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LostLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only nonartifact, nonland card names")
    void offersOnlyAllowedCardNames() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Ornithopter(), new Forest())));
        harness.setHand(player1, List.of(new LostLegacy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Grizzly Bears");
        assertThat(choice.options()).doesNotContain("Ornithopter", "Forest");
    }

    @Test
    @DisplayName("Exiles matching cards from all zones and draws for hand cards exiled")
    void exilesMatchingCardsAndDrawsForHandCopies() {
        Card handBears1 = new GrizzlyBears();
        Card handBears2 = new GrizzlyBears();
        Card graveyardBears = new GrizzlyBears();
        Card libraryBears = new GrizzlyBears();
        Card handPeek = new Peek();
        Card libraryPeek1 = new Peek();
        Card libraryPeek2 = new Peek();

        harness.setHand(player2, new ArrayList<>(List.of(handBears1, handBears2, handPeek)));
        harness.setGraveyard(player2, List.of(graveyardBears));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(libraryBears, libraryPeek1, libraryPeek2));

        harness.setHand(player1, List.of(new LostLegacy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1,
                List.of(handBears1.getId(), handBears2.getId(), graveyardBears.getId(), libraryBears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(
                handBears1, handBears2, graveyardBears, libraryBears);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Grizzly Bears")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Peek")).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw for cards exiled from the graveyard or library")
    void drawsOnlyForHandCopies() {
        Card graveyardBears = new GrizzlyBears();
        Card libraryBears = new GrizzlyBears();
        Card handPeek = new Peek();

        harness.setHand(player2, new ArrayList<>(List.of(handPeek)));
        harness.setGraveyard(player2, List.of(graveyardBears));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(libraryBears);

        harness.setHand(player1, List.of(new LostLegacy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(graveyardBears.getId(), libraryBears.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handPeek);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new LostLegacy(), bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }
}
