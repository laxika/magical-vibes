package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicTutor.class, GrizzlyBears.class})
class DemonicTutorTest extends BaseCardTest {

    @Test
    void searchesLibraryForAnyCardAndPutsItIntoHand() {
        Card tutor = new DemonicTutor();
        Card chosenCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new DemonicTutor(), chosenCard, new GrizzlyBears()));
        harness.castFromHand(player1, tutor, "{1}{B}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrderElementsOf(
                gd.playerDecks.get(player1.getId()));
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().reveals()).isFalse();

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(chosenIndex));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(tutor.getId()));
    }

    @Test
    void unrestrictedSearchCannotFailToFind() {
        DemonicTutor tutor = new DemonicTutor();
        Card chosenCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosenCard));
        harness.castFromHand(player1, tutor, "{1}{B}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThatThrownBy(() -> harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(tutor.getId()));
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void emptyLibrarySearchDoesNotPrompt() {
        DemonicTutor tutor = new DemonicTutor();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, tutor, "{1}{B}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(tutor.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(tutor.getId()));
        assertThat(gameLogContains("it is empty. Library is shuffled.")).isTrue();
    }

}
