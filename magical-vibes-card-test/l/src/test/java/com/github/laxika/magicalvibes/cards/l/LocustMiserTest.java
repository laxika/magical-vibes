package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LocustMiser.class, Humility.class})
class LocustMiserTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces each opponent's maximum hand size by two")
    void reducesOpponentsMaximumHandSize() {
        harness.addToBattlefield(player1, new LocustMiser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfEightCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not reduce the controller's maximum hand size")
    void doesNotReduceControllersMaximumHandSize() {
        harness.addToBattlefield(player1, new LocustMiser());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Multiple Locust Misers reduce maximum hand size cumulatively")
    void multipleMisersStack() {
        harness.addToBattlefield(player1, new LocustMiser());
        harness.addToBattlefield(player1, new LocustMiser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("A maximum hand size below zero requires discarding the entire hand")
    void reductionBelowZeroDiscardsEntireHand() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new LocustMiser());
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("Locust Miser has no hand-size effect when Humility removes its ability")
    void losingAbilitiesStopsHandSizeReduction() {
        harness.addToBattlefield(player1, new LocustMiser());
        harness.addToBattlefield(player1, new Humility());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfSevenCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent at the reduced maximum hand size does not discard")
    void noDiscardAtReducedMaximum() {
        harness.addToBattlefield(player1, new LocustMiser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(
                new LocustMiser(), new LocustMiser(), new LocustMiser(),
                new LocustMiser(), new LocustMiser()));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    private List<Card> handOfEightCards() {
        return new ArrayList<>(List.of(
                new LocustMiser(), new LocustMiser(), new LocustMiser(), new LocustMiser(),
                new LocustMiser(), new LocustMiser(), new LocustMiser(), new LocustMiser()
        ));
    }

    private List<Card> handOfSevenCards() {
        return new ArrayList<>(List.of(
                new LocustMiser(), new LocustMiser(), new LocustMiser(), new LocustMiser(),
                new LocustMiser(), new LocustMiser(), new LocustMiser()
        ));
    }
}
