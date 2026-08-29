package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FerventMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting searches up to three cards, then discards three at random")
    void normalCastSearchesThenDiscardsAtRandom() {
        Card searchedOne = new Plains();
        Card searchedTwo = new Swamp();
        Card searchedThree = new GrizzlyBears();
        Card libraryRemainder = new Plains();
        harness.setHand(player1, List.of(new FerventMastery()));
        harness.setLibrary(player1, List.of(searchedOne, searchedTwo, searchedThree, libraryRemainder));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryRemainder);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(searchedOne, searchedTwo, searchedThree)
                .extracting(Card::getName)
                .contains("Fervent Mastery");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Alternate casting lets the opponent choose a rummage before the search")
    void alternateCastOpponentRummagesThenSearches() {
        Card opponentDiscardOne = new Plains();
        Card opponentDiscardTwo = new Swamp();
        Card opponentKeep = new GrizzlyBears();
        Card opponentDrawOne = new Plains();
        Card opponentDrawTwo = new Swamp();
        Card searchedOne = new Plains();
        Card searchedTwo = new Swamp();
        Card searchedThree = new GrizzlyBears();
        harness.setHand(player1, List.of(new FerventMastery()));
        harness.setHand(player2, List.of(opponentDiscardOne, opponentDiscardTwo, opponentKeep));
        harness.setLibrary(player1, List.of(searchedOne, searchedTwo, searchedThree));
        harness.setLibrary(player2, List.of(opponentDrawOne, opponentDrawTwo));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
