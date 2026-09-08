package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreepingDreadTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent who discards a card sharing a type loses 3 life")
    void matchingDiscardedTypeCausesLifeLoss() {
        resolveWithHands(List.of(new GrizzlyBears()), List.of(new GrizzlyBears()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opponent with a nonmatching discarded type does not lose life")
    void nonmatchingDiscardedTypeDoesNotCauseLifeLoss() {
        resolveWithHands(List.of(new GrizzlyBears()), List.of(new Forest()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No life is lost when the controller has no card to discard")
    void noControllerDiscardMeansNoLifeLoss() {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void resolveWithHands(List<com.github.laxika.magicalvibes.model.Card> controllerHand,
                                  List<com.github.laxika.magicalvibes.model.Card> opponentHand) {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, new ArrayList<>(controllerHand));
        harness.setHand(player2, new ArrayList<>(opponentHand));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
    }
}
