package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NeurokFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact card revealed on top goes to hand")
    void artifactCardGoesToHand() {
        Card artifact = card("Test Artifact", CardType.ARTIFACT);
        harness.setLibrary(player1, List.of(artifact));

        castNeurokFamiliar();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Non-artifact card revealed on top goes to the graveyard")
    void nonArtifactCardGoesToGraveyard() {
        Card nonArtifact = card("Test Sorcery", CardType.SORCERY);
        harness.setLibrary(player1, List.of(nonArtifact));

        castNeurokFamiliar();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonArtifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonArtifact);
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castNeurokFamiliar();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castNeurokFamiliar() {
        harness.setHand(player1, List.of(new NeurokFamiliar()));
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
