package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FerventMastery.class, Plains.class, Swamp.class, GrizzlyBears.class})
class FerventMasteryTest extends BaseCardTest {

    @Test
    void maySearchForZeroCardsAndStillDiscard() {
        Card retained = new Plains();
        Card discarded = new Swamp();
        harness.setHand(player1, List.of(new FerventMastery(), discarded));
        harness.setLibrary(player1, List.of(retained));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayStopSearchingAfterOneCard() {
        Card selected = new Plains();
        Card retained = new Swamp();
        harness.setHand(player1, List.of(new FerventMastery()));
        harness.setLibrary(player1, List.of(selected, retained));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(selected);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void alternateCostOpponentMayDiscardZeroWithEmptyCasterLibrary() {
        Card opponentHand = new Plains();
        Card opponentLibrary = new Swamp();
        Card casterHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new FerventMastery(), casterHand));
        harness.setHand(player2, List.of(opponentHand));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opponentLibrary));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibrary);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(casterHand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryStillDiscardsThreeFromEntireHand() {
        Card selected = new Plains();
        Card existingOne = new Swamp();
        Card existingTwo = new GrizzlyBears();
        Card existingThree = new Plains();
        harness.setHand(player1, List.of(new FerventMastery(), existingOne, existingTwo, existingThree));
        harness.setLibrary(player1, List.of(selected));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(card -> List.of(selected, existingOne, existingTwo, existingThree).contains(card));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

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

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

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
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
