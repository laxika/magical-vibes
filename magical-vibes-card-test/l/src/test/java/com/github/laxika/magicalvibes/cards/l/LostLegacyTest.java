package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({LostLegacy.class, GrizzlyBears.class, Ornithopter.class, Peek.class, Forest.class})
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
        harness.setLibrary(player2, List.of(libraryBears, libraryPeek1, libraryPeek2));

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
        harness.setLibrary(player2, List.of(libraryBears));

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

    @Test
    @DisplayName("May choose zero matching cards without drawing")
    void mayExileZeroCards() {
        Card handBears = new GrizzlyBears();
        Card graveyardBears = new GrizzlyBears();
        Card libraryBears = new GrizzlyBears();
        harness.setHand(player2, List.of(handBears));
        harness.setGraveyard(player2, List.of(graveyardBears));
        harness.setLibrary(player2, List.of(libraryBears));
        harness.setHand(player1, List.of(new LostLegacy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardBears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryBears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May leave matching copies in every searched zone and draws only for selected hand cards")
    void mayExileOnlySomeCopies() {
        Card selectedBears = new GrizzlyBears();
        Card remainingBears = new GrizzlyBears();
        Card graveyardBears = new GrizzlyBears();
        Card libraryBears = new GrizzlyBears();
        harness.setHand(player2, List.of(selectedBears, remainingBears));
        harness.setGraveyard(player2, List.of(graveyardBears));
        harness.setLibrary(player2, List.of(libraryBears));
        harness.setHand(player1, List.of(new LostLegacy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(selectedBears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(selectedBears);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(remainingBears, libraryBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardBears);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Naming a card absent from the target's zones resolves without drawing")
    void mayNameAbsentCard() {
        Card peek = new Peek();
        Card forest = new Forest();
        harness.setHand(player1, List.of(new LostLegacy(), new GrizzlyBears()));
        harness.setHand(player2, List.of(peek));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(peek);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }
}
