package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BolassCitadel;
import com.github.laxika.magicalvibes.cards.m.MoldgrafMillipede;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiredIdea.class, MoldgrafMillipede.class, BolassCitadel.class})
class InspiredIdeaTest extends BaseCardTest {

    private List<Card> library() {
        return new ArrayList<>(List.of(new MoldgrafMillipede(), new MoldgrafMillipede(), new MoldgrafMillipede()));
    }

    @Test
    @DisplayName("Normal cast draws three cards and reduces the controller's maximum hand size")
    void normalCastDrawsAndReducesMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library());
        harness.castFromHand(player1, new InspiredIdea(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        gd.playerHands.get(player1.getId()).addAll(List.of(new MoldgrafMillipede(), new MoldgrafMillipede()));

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Cleave draws three cards without reducing the maximum hand size")
    void cleaveDrawsWithoutReducingMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library());
        harness.setHand(player1, List.of(new InspiredIdea()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        gd.playerHands.get(player1.getId()).addAll(List.of(new MoldgrafMillipede(), new MoldgrafMillipede()));
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNull();
    }

    @Test
    void normalCastReductionPersistsAfterCleanup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library());
        harness.castFromHand(player1, new InspiredIdea(), "{2}{U}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();

        gd.playerHands.get(player1.getId()).addAll(List.of(new MoldgrafMillipede(), new MoldgrafMillipede()));
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    void twoNormalCastsReduceMaximumHandSizeToOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Card> deck = library();
        deck.addAll(library());
        harness.setLibrary(player1, deck);
        harness.setHand(player1, List.of(new InspiredIdea(), new InspiredIdea()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(5);
    }

    @Test
    void normalCastDoesNotReduceOpponentsMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library());
        harness.castFromHand(player1, new InspiredIdea(), "{2}{U}");
        harness.passBothPriorities();
        List<Card> opponentHand = library();
        opponentHand.addAll(library());
        opponentHand.add(new MoldgrafMillipede());
        harness.setHand(player2, opponentHand);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @CardUsed({InspiredIdea.class, MoldgrafMillipede.class, BolassCitadel.class})
    void payingLifeWithCitadelDoesNotRemoveHandSizeReduction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BolassCitadel());
        List<Card> deck = library();
        deck.addFirst(new InspiredIdea());
        harness.setLibrary(player1, deck);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertLife(player1, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        gd.playerHands.get(player1.getId()).addAll(List.of(new MoldgrafMillipede(), new MoldgrafMillipede()));
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);
    }
}
