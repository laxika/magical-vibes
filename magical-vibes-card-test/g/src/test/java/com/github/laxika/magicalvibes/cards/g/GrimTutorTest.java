package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimTutor.class, MindRot.class, GrizzlyBears.class, Island.class})
class GrimTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any card from the library")
    void offersAnyCard() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and loses 3 life")
    void choosingCardPutsItIntoHandAndLosesLife() {
        harness.setLife(player1, 20);
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.get(1).getName();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals(chosenName));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Life is lost only after the library search is completed")
    void losesLifeAfterSearching() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Island chosen = new Island();
        harness.setLibrary(player1, List.of(chosen));
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still causes the controller to lose 3 life")
    void emptyLibraryStillLosesLife() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        cast();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GrimTutor);
    }

    @Test
    @DisplayName("An unrestricted search cannot fail to find a card in a nonempty library")
    void cannotDeclineToFind() {
        Island chosen = new Island();
        harness.setLibrary(player1, List.of(chosen));
        cast();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.castFromHand(player1, new GrimTutor(), "{1}{B}{B}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new MindRot(), new GrizzlyBears(), new Island()));
    }
}
