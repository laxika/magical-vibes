package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeurokFamiliar.class, AetherSpellbomb.class, Ornithopter.class, Annul.class})
class NeurokFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact card revealed on top goes to hand")
    void artifactCardGoesToHand() {
        AetherSpellbomb artifact = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(artifact));

        castNeurokFamiliar();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Artifact creature revealed on top goes to hand")
    void artifactCreatureGoesToHand() {
        Ornithopter artifactCreature = new Ornithopter();
        harness.setLibrary(player1, List.of(artifactCreature));

        castNeurokFamiliar();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(artifactCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifactCreature);
    }

    @Test
    @DisplayName("Non-artifact card revealed on top goes to the graveyard")
    void nonArtifactCardGoesToGraveyard() {
        Annul nonArtifact = new Annul();
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
        harness.castFromHand(player1, new NeurokFamiliar(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
