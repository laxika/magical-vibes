package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.d.DarksteelPendant;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromescaleDrake.class, CrazedGoblin.class, DarksteelIngot.class,
        DarksteelPendant.class, EchoingTruth.class})
class ChromescaleDrakeTest extends BaseCardTest {

    private void castDrake() {
        harness.setHand(player1, List.of(new ChromescaleDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Artifact cards among the top three go to hand")
    void artifactCardsGoToHand() {
        DarksteelIngot artifact1 = new DarksteelIngot();
        CrazedGoblin creature = new CrazedGoblin();
        DarksteelPendant artifact2 = new DarksteelPendant();
        harness.setLibrary(player1, List.of(artifact1, creature, artifact2));

        castDrake();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact1, artifact2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Non-artifact cards among the top three go to the graveyard")
    void nonArtifactCardsGoToGraveyard() {
        EchoingTruth instant = new EchoingTruth();
        CrazedGoblin creature = new CrazedGoblin();
        DarksteelIngot artifact = new DarksteelIngot();
        harness.setLibrary(player1, List.of(instant, creature, artifact));

        castDrake();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Cards below the top three stay in the library")
    void onlyTopThreeAreProcessed() {
        EchoingTruth instant = new EchoingTruth();
        DarksteelIngot artifact = new DarksteelIngot();
        CrazedGoblin creature = new CrazedGoblin();
        DarksteelPendant deepArtifact = new DarksteelPendant();
        harness.setLibrary(player1, List.of(instant, artifact, creature, deepArtifact));

        castDrake();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).contains(deepArtifact);
    }

    @Test
    @DisplayName("Processes all cards when the library has fewer than three cards")
    void processesShortLibrary() {
        DarksteelIngot artifact = new DarksteelIngot();
        EchoingTruth nonArtifact = new EchoingTruth();
        harness.setLibrary(player1, List.of(artifact, nonArtifact));

        castDrake();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        castDrake();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
