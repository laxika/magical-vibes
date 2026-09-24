package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OppositionAgent.class, DiabolicTutor.class, GrizzlyBears.class, Shock.class})
class OppositionAgentTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Controls an opponent's search and exiles the card they find for its controller to play")
    void controlsOpponentSearch() {
        harness.addToBattlefield(player1, new OppositionAgent());
        Shock chosenCard = new Shock();
        harness.setLibrary(player2, List.of(chosenCard, new GrizzlyBears()));
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(search.params().playerId()).isEqualTo(player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(chosenCard);
        assertThat(gd.findExiledCard(chosenCard.getId())).satisfies(entry -> {
            assertThat(entry).isNotNull();
            assertThat(entry.faceDown()).isFalse();
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
        });
        assertThat(gd.exilePlayPermissions).containsEntry(chosenCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosenCard.getId());
    }
}
