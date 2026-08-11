package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Mills thirteen cards from a target opponent's library")
    void millsThirteenCards() {
        harness.setLibrary(player2, libraryOfThirteen());
        harness.setHand(player1, List.of(new ArchiveTrap()));
        addArchiveTrapMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(13);
    }

    @Test
    @DisplayName("Cannot use the free alternate cost before an opponent searches")
    void alternateCostRequiresOpponentSearch() {
        harness.setHand(player1, List.of(new ArchiveTrap()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the free alternate cost after an opponent searches their library")
    void usesFreeAlternateCostAfterOpponentSearches() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new RampantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        harness.setLibrary(player2, libraryOfThirteen());
        harness.setHand(player1, List.of(new ArchiveTrap()));
        harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(13);
    }

    @Test
    @DisplayName("Only targets an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ArchiveTrap()));
        addArchiveTrapMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addArchiveTrapMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Card> libraryOfThirteen() {
        return IntStream.range(0, 13)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
