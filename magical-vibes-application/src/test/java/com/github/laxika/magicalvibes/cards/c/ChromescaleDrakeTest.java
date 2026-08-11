package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromescaleDrakeTest extends BaseCardTest {

    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void castDrake() {
        harness.setHand(player1, List.of(new ChromescaleDrake()));
        harness.addMana(player1, ManaColor.BLUE, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Artifact cards among the top three go to hand")
    void artifactCardsGoToHand() {
        Card artifact1 = createCard("Spellbook", CardType.ARTIFACT);
        Card creature = createCard("Grizzly Bears", CardType.CREATURE);
        Card artifact2 = createCard("Prophetic Prism", CardType.ARTIFACT);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(artifact1, creature, artifact2));

        castDrake();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact1, artifact2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Non-artifact cards among the top three go to the graveyard")
    void nonArtifactCardsGoToGraveyard() {
        Card land = createCard("Island", CardType.LAND);
        Card instant = createCard("Shock", CardType.INSTANT);
        Card artifact = createCard("Spellbook", CardType.ARTIFACT);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(land, instant, artifact));

        castDrake();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Cards below the top three stay in the library")
    void onlyTopThreeAreProcessed() {
        Card land = createCard("Island", CardType.LAND);
        Card artifact = createCard("Spellbook", CardType.ARTIFACT);
        Card instant = createCard("Shock", CardType.INSTANT);
        Card deepArtifact = createCard("Darksteel Ingot", CardType.ARTIFACT);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(land, artifact, instant, deepArtifact));

        castDrake();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepArtifact);
        assertThat(deck).contains(deepArtifact);
    }
}
