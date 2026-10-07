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

    @Test
    @DisplayName("Opponent may lose life even when sacrifice and discard are available")
    void opponentMayDeclineBothAlternatives() {
        harness.addToBattlefield(player1, new StarseerMentor());
        harness.addToBattlefield(player2, new StarseerMentor());
        harness.setHand(player2, List.of(new Forest()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lose 3 life");

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Starseer Mentor");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Lands cannot be sacrificed to prevent the life loss")
    void landsDoNotProvideSacrificeAlternative() {
        harness.addToBattlefield(player1, new StarseerMentor());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent life changes do not satisfy the controller's condition")
    void opponentLifeChangesDoNotTrigger() {
        harness.addToBattlefield(player1, new StarseerMentor());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        harness.addToBattlefield(player1, new StarseerMentor());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gaining and losing life in the same turn produces only one trigger")
    void bothLifeChangesProduceOneTrigger() {
        harness.addToBattlefield(player1, new StarseerMentor());
        harness.setHand(player2, List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        chooseOpponentTarget();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
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
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
