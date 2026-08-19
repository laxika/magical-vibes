package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.passBothPriorities();
        harness.passBothPriorities();
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
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, filler);
    }
}
