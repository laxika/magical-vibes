package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MyrConvert;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtraxaGrandUnifier.class, Island.class, MyrConvert.class, PropheticPrism.class})
class AtraxaGrandUnifierTest extends BaseCardTest {

    private static Card card(String name, CardType type, CardType... additionalTypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setAdditionalTypes(Set.of(additionalTypes));
        return card;
    }

    private static Card untypedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private void castAtraxa(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AtraxaGrandUnifier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("ETB offers at most one card for each represented card type")
    void offersOneCardForEachCardType() {
        Card artifactCreature = card("Artifact Creature", CardType.ARTIFACT, CardType.CREATURE);
        Card artifact = card("Artifact", CardType.ARTIFACT);
        Card enchantment = card("Enchantment", CardType.ENCHANTMENT);
        Card instant = card("Instant", CardType.INSTANT);
        Card land = card("Land", CardType.LAND);
        Card planeswalker = card("Planeswalker", CardType.PLANESWALKER);
        Card sorcery = card("Sorcery", CardType.SORCERY);
        Card battle = card("Battle", CardType.BATTLE);
        Card filler1 = untypedCard("Filler 1");
        Card filler2 = untypedCard("Filler 2");
        List<Card> library = List.of(artifactCreature, artifact, enchantment, instant, land,
                planeswalker, sorcery, battle, filler1, filler2);

        castAtraxa(library);

        int selections = 0;
        while (gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null) {
            harness.handleCardChosen(player1, 0);
            selections++;
        }

        assertThat(selections).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(
                artifactCreature, artifact, enchantment, instant, land, planeswalker, sorcery, battle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(filler1, filler2);
    }

    @Test
    @DisplayName("Declining a type leaves all unchosen revealed cards on the library bottom")
    void decliningSelectionBottomsAllRevealedCards() {
        Card land = card("Land", CardType.LAND);
        Card filler = untypedCard("Filler");
        List<Card> library = List.of(land, filler);

        castAtraxa(library);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, filler);
    }

    @Test
    @DisplayName("Declining the creature pick still allows the same artifact creature as the artifact pick")
    void decliningOneTypeDoesNotDeclineLaterTypes() {
        Card myr = new MyrConvert();
        castAtraxa(List.of(myr));

        harness.handleCardChosen(player1, -1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(myr);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Two artifact creatures can be selected, one for each of their types")
    void selectsDifferentCardsForOverlappingTypes() {
        Card first = new MyrConvert();
        Card second = new MyrConvert();
        castAtraxa(List.of(first, second));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Selecting an artifact creature once does not offer it again for another type")
    void doesNotSelectTheSameCardTwice() {
        Card myr = new MyrConvert();
        castAtraxa(List.of(myr));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(myr);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the top ten cards are revealed and the rest stay above the unchosen cards")
    void bottomsOnlyTheRevealedCards() {
        List<Card> revealed = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            revealed.add(new Island());
        }
        Card unrevealed = new PropheticPrism();
        List<Card> library = new ArrayList<>(revealed);
        library.add(unrevealed);
        castAtraxa(library);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed.getFirst());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 10))
                .containsExactlyInAnyOrderElementsOf(revealed.subList(1, 10));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library creates no selection and does not cause a failed draw")
    void emptyLibraryFinishesWithoutInteraction() {
        castAtraxa(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Atraxa, Grand Unifier");
    }
}
