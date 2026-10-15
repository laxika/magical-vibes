package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnatMiser.class, Humility.class})
class GnatMiserTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces each opponent's maximum hand size by one")
    void reducesOpponentsMaximumHandSize() {
        harness.addToBattlefield(player1, new GnatMiser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not reduce the controller's maximum hand size")
    void doesNotReduceControllersMaximumHandSize() {
        harness.addToBattlefield(player1, new GnatMiser());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not require a discard when an opponent has six cards")
    void doesNotRequireDiscardAtReducedMaximum() {
        harness.addToBattlefield(player1, new GnatMiser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(
                new GnatMiser(), new GnatMiser(), new GnatMiser(),
                new GnatMiser(), new GnatMiser(), new GnatMiser()
        ));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Multiple Gnat Misers reduce the opponent's hand size cumulatively")
    void multipleMisersStack() {
        harness.addToBattlefield(player1, new GnatMiser());
        harness.addToBattlefield(player1, new GnatMiser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A maximum hand size reduced below zero requires discarding only the whole hand")
    void reductionsBelowZeroDiscardEntireHand() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new GnatMiser());
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(7);
    }

    @Test
    @DisplayName("Gnat Miser has no hand-size effect while Humility removes its ability")
    void abilityRemovalStopsHandSizeReduction() {
        harness.addToBattlefield(player1, new GnatMiser());
        harness.addToBattlefield(player1, new Humility());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    private List<Card> handOfSevenCards() {
        return new ArrayList<>(List.of(
                new GnatMiser(), new GnatMiser(), new GnatMiser(),
                new GnatMiser(), new GnatMiser(), new GnatMiser(),
                new GnatMiser()
        ));
    }
}
