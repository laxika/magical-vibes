package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    @DisplayName("Trigger uses the top card at resolution and leaves the rest of the library untouched")
    void revealsCurrentTopCardAtResolution() {
        AetherSpellbomb originalTop = new AetherSpellbomb();
        Annul currentTop = new Annul();
        Ornithopter remainingCard = new Ornithopter();
        harness.setLibrary(player1, List.of(originalTop, remainingCard));
        harness.setLibrary(player2, List.of(new AetherSpellbomb()));

        harness.castFromHand(player1, new NeurokFamiliar(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop, remainingCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.setLibrary(player1, List.of(currentTop, originalTop, remainingCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(currentTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop, remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Trigger still resolves after Familiar returns to hand")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        AetherSpellbomb revealedCard = new AetherSpellbomb();
        NeurokFamiliar familiar = new NeurokFamiliar();
        harness.setLibrary(player1, List.of(revealedCard));
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.castFromHand(player1, familiar, "{1}{U}");
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null,
                harness.getPermanentId(player1, "Neurok Familiar"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(familiar);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealedCard);
        harness.assertNotOnBattlefield(player1, "Neurok Familiar");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(familiar, revealedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castNeurokFamiliar() {
        harness.castFromHand(player1, new NeurokFamiliar(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
