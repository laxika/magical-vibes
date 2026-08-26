package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ArcanistsOwl.class)
class ArcanistsOwlTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers artifact and enchantment cards among the top four")
    void offersArtifactsAndEnchantments() {
        Card artifact = card("Artifact", CardType.ARTIFACT);
        Card enchantment = card("Enchantment", CardType.ENCHANTMENT);
        Card creature = card("Creature", CardType.CREATURE);
        Card instant = card("Instant", CardType.INSTANT);
        setUpAndResolve(List.of(artifact, enchantment, creature, instant));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and the rest on the bottom")
    void choosingCardPutsItIntoHand() {
        Card artifact = card("Artifact", CardType.ARTIFACT);
        Card enchantment = card("Enchantment", CardType.ENCHANTMENT);
        Card creature = card("Creature", CardType.CREATURE);
        Card instant = card("Instant", CardType.INSTANT);
        setUpAndResolve(List.of(artifact, enchantment, creature, instant));

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                artifact, creature, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no artifact or enchantment, the top four go straight to the bottom")
    void noMatchingCardNeedsNoChoice() {
        Card creature = card("Creature", CardType.CREATURE);
        Card instant = card("Instant", CardType.INSTANT);
        Card sorcery = card("Sorcery", CardType.SORCERY);
        Card land = card("Land", CardType.LAND);
        setUpAndResolve(List.of(creature, instant, sorcery, land));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                creature, instant, sorcery, land);
    }

    private void setUpAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ArcanistsOwl()));
        harness.addMana(player1, ManaColor.WHITE, 4);

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
