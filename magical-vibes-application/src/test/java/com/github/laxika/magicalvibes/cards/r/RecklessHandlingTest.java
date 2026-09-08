package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

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
}
