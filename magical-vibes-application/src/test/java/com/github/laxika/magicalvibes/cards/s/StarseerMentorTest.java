package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarseerMentor.class, Forest.class, GrizzlyBears.class})
class StarseerMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when its controller neither gained nor lost life")
    void doesNotTriggerWithoutLifeChange() {
        harness.addToBattlefield(player1, new StarseerMentor());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targets an opponent and causes them to lose 3 life after life gain")
    void triggersAfterLifeGain() {
        harness.addToBattlefield(player1, new StarseerMentor());
        harness.setHand(player2, List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Causes an opponent to lose 3 life after life loss")
    void triggersAfterLifeLoss() {
        harness.addToBattlefield(player1, new StarseerMentor());
        harness.setHand(player2, List.of());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Targeted opponent may sacrifice a nonland permanent")
    void opponentMaySacrificeNonlandPermanent() {
        harness.addToBattlefield(player1, new StarseerMentor());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        harness.passBothPriorities();
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Targeted opponent may discard a card")
    void opponentMayDiscardCard() {
        harness.addToBattlefield(player1, new StarseerMentor());
        harness.setHand(player2, List.of(new Forest()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        harness.passBothPriorities();
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Forest");
    }

    private void chooseOpponentTarget() {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
