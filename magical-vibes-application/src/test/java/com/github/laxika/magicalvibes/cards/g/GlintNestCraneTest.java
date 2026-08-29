package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlintNestCraneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only artifact cards among the top four")
    void etbOffersOnlyArtifacts() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        setupTopCards(List.of(artifact, card("Test Creature", CardType.CREATURE),
                card("Test Land", CardType.LAND), card("Test Sorcery", CardType.SORCERY)));

        castAndResolveEtb();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(artifact);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand and prompts to order the rest on the bottom")
    void choosingArtifactPutsItIntoHand() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        Card creature = card("Test Creature", CardType.CREATURE);
        Card land = card("Test Land", CardType.LAND);
        Card sorcery = card("Test Sorcery", CardType.SORCERY);
        setupTopCards(List.of(artifact, creature, land, sorcery));

        castAndResolveEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrder(creature, land, sorcery);
    }

    @Test
    @DisplayName("With no artifact among the top four, all cards go to the bottom")
    void noArtifactNeedsNoCardChoice() {
        List<Card> topCards = List.of(card("Test Creature", CardType.CREATURE), card("Test Land", CardType.LAND),
                card("Test Sorcery", CardType.SORCERY), card("Test Instant", CardType.INSTANT));
        setupTopCards(topCards);

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new GlintNestCrane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
