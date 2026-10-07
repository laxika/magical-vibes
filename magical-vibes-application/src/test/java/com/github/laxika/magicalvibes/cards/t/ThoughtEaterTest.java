package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtEater.class, Forest.class, Mountain.class, Plains.class})
class ThoughtEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Controller must discard down to four during cleanup")
    void controllerMaximumHandSizeIsReducedByThree() {
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Mountain(), new Plains(), new Forest()
        )));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Thought Eater does not reduce an opponent's maximum hand size")
    void opponentMaximumHandSizeIsUnaffected() {
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Mountain(), new Plains(), new Plains()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controller's maximum hand size returns to seven when Thought Eater leaves")
    void reductionEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new ThoughtEater());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Mountain(), new Plains(), new Plains()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Two Thought Eaters reduce the controller's maximum hand size to one")
    void multipleReductionsStack() {
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Mountain()));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A negative maximum hand size requires discarding the entire hand")
    void reductionsCannotRequireMoreDiscardsThanCardsInHand() {
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({Humility.class})
    @DisplayName("Humility removes Thought Eater's maximum hand size reduction")
    void losingAbilitiesRemovesHandSizeReduction() {
        harness.addToBattlefield(player1, new ThoughtEater());
        harness.addToBattlefield(player2, new Humility());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Mountain(), new Plains(), new Plains()
        ));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }
}
