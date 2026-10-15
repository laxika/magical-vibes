package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AstarionTheDecadent.class})
class AstarionTheDecadentTest extends BaseCardTest {

    private static final String FEED =
            "Feed — Target opponent loses life equal to the amount of life they lost this turn.";
    private static final String FRIENDS =
            "Friends — You gain life equal to the amount of life you gained this turn.";

    @Test
    void feedMakesTargetOpponentLoseLifeTheyLostThisTurn() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        harness.setLife(player2, 20);
        gd.lifeLostThisTurn.put(player1.getId(), 7);
        gd.lifeLostThisTurn.put(player2.getId(), 3);

        resolveEndStep(player1);
        harness.handleListChoice(player1, FEED);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void feedCannotTargetItsController() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        gd.lifeLostThisTurn.put(player2.getId(), 3);

        resolveEndStep(player1);
        harness.handleListChoice(player1, FEED);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void friendsMakesItsControllerGainLifeTheyGainedThisTurn() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        int lifeBefore = gd.getLife(player1.getId());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);
        gd.lifeGainedThisTurn.put(player2.getId(), 9);

        resolveEndStep(player1);
        harness.handleListChoice(player1, FRIENDS);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    void feedCountsAllLifeLostIncludingLossBeforeResolutionWithoutSubtractingGains() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 3, "test"));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 2));

        resolveEndStep(player1);
        harness.handleListChoice(player1, FEED);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 2, "test"));
        int lifeBeforeResolution = gd.getLife(player2.getId());
        int controllerLife = gd.getLife(player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBeforeResolution - 5);
        harness.assertLife(player1, controllerLife);
    }

    @Test
    void friendsCountsAllLifeGainedIncludingGainBeforeResolutionWithoutSubtractingLosses() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "test"));

        resolveEndStep(player1);
        harness.handleListChoice(player1, FRIENDS);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        int lifeBeforeResolution = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBeforeResolution + 6);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    void feedCanTargetOpponentWhoLostNoLife() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, FEED);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    void friendsCanBeChosenWhenNoLifeWasGained() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        int lifeBefore = gd.getLife(player1.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, FRIENDS);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new AstarionTheDecadent());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
