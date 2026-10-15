package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessHandling.class, FountainOfYouth.class, Mountain.class})
class RecklessHandlingTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for an artifact, then damages each opponent when it is discarded")
    void artifactDiscardDealsDamageToEachOpponent() {
        harness.setHand(player1, List.of(new RecklessHandling()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().getFirst().getName()).isEqualTo("Fountain of Youth");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gameData.getLife(player2.getId())).isEqualTo(18);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still discards when no artifact is found, but deals no damage")
    void nonArtifactDiscardDealsNoDamage() {
        harness.setHand(player1, List.of(new RecklessHandling(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(20);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find still discards an artifact already in hand and deals damage")
    void failingToFindStillDiscardsExistingArtifact() {
        harness.setHand(player1, List.of(new RecklessHandling(), new FountainOfYouth()));
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still permits discarding an artifact from hand")
    void emptyLibraryStillDiscardsArtifact() {
        harness.setHand(player1, List.of(new RecklessHandling(), new FountainOfYouth()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find with an empty hand deals no damage")
    void failingToFindWithEmptyHandDealsNoDamage() {
        harness.setHand(player1, List.of(new RecklessHandling()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Random discard removes exactly one card without asking for a discard choice")
    void randomDiscardRemovesExactlyOneCard() {
        harness.setHand(player1, List.of(new RecklessHandling(), new FountainOfYouth()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Fountain of Youth"))).hasSize(1);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
