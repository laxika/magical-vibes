package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DimirLocket;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GatewayPlaza;
import com.github.laxika.magicalvibes.cards.h.HealersHawk;
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

@CardUsed({UnmooredEgo.class, HealersHawk.class, DimirLocket.class, GatewayPlaza.class, Forest.class})
class UnmooredEgoTest extends BaseCardTest {

    @Test
    @DisplayName("Allows creature, artifact, and basic and nonbasic land card names")
    void offersAllCardTypes() {
        harness.setHand(player2, List.of(new HealersHawk(), new DimirLocket(), new Forest(), new GatewayPlaza()));
        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Healer's Hawk", "Dimir Locket", "Forest", "Gateway Plaza");
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
        Card handHawk1 = new HealersHawk();
        Card handHawk2 = new HealersHawk();
        Card graveyardHawk1 = new HealersHawk();
        Card graveyardHawk2 = new HealersHawk();
        Card graveyardHawk3 = new HealersHawk();
        Card graveyardHawk4 = new HealersHawk();
        Card handGatewayPlaza = new GatewayPlaza();
        Card libraryGatewayPlaza1 = new GatewayPlaza();
        Card libraryGatewayPlaza2 = new GatewayPlaza();

        harness.setHand(player2, List.of(handHawk1, handHawk2, handGatewayPlaza));
        harness.setGraveyard(player2,
                List.of(graveyardHawk1, graveyardHawk2, graveyardHawk3, graveyardHawk4));
        harness.setLibrary(player2, List.of(libraryGatewayPlaza1, libraryGatewayPlaza2));

        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Healer's Hawk");

        List<java.util.UUID> fiveCards = List.of(
                handHawk1.getId(), handHawk2.getId(), graveyardHawk1.getId(),
                graveyardHawk2.getId(), graveyardHawk3.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, fiveCards))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Choose at most 4 cards");

        harness.handleMultipleCardsChosen(player1, List.of(
                handHawk1.getId(), handHawk2.getId(), graveyardHawk1.getId(), graveyardHawk2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(
                handHawk1, handHawk2, graveyardHawk1, graveyardHawk2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardHawk3, graveyardHawk4);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Healer's Hawk")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Gateway Plaza")).hasSize(3);
    }

    @Test
    @DisplayName("Searches all three zones and draws only for the selected hand copy")
    void exilesFromAllThreeZonesAndDrawsOnlyForHand() {
        Card handCopy = new HealersHawk();
        Card unselectedHandCopy = new HealersHawk();
        Card graveyardCopy = new HealersHawk();
        Card libraryCopy = new HealersHawk();
        Card drawnCard = new GatewayPlaza();
        harness.setHand(player2, List.of(handCopy, unselectedHandCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy, drawnCard));
        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Healer's Hawk");
        harness.handleMultipleCardsChosen(player1,
                List.of(handCopy.getId(), graveyardCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(handCopy, graveyardCopy, libraryCopy);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(unselectedHandCopy, drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("May exile zero matching cards without drawing")
    void mayChooseZeroCards() {
        Card handCopy = new HealersHawk();
        Card graveyardCopy = new HealersHawk();
        Card libraryCopy = new HealersHawk();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Healer's Hawk");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A name absent from the opponent's zones resolves without exiling or drawing")
    void noMatchingCards() {
        Card handCard = new HealersHawk();
        Card libraryCard = new GatewayPlaza();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new UnmooredEgo()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Unmoored Ego");

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
