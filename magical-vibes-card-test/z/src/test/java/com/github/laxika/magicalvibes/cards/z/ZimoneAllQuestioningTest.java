package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneAllQuestioning.class, Forest.class})
class ZimoneAllQuestioningTest extends BaseCardTest {

    @Test
    void createsLegendaryFractalWithCountersEqualToControlledLands() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        resolveControllerEndStep();

        Permanent primo = findPermanent(player1, "Primo, the Indivisible");
        assertThat(primo.getCard().isToken()).isTrue();
        assertThat(primo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(primo.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(primo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(primo.getEffectivePower()).isEqualTo(2);
        assertThat(primo.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForACompositeNumberOfLands() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        for (int i = 0; i < 4; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
        }

        resolveControllerEndStep();

        harness.assertNotOnBattlefield(player1, "Primo, the Indivisible");
    }

    @Test
    void doesNotResolveIfTheLandCountStopsBeingPrime() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().hasType(CardType.LAND));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primo, the Indivisible");
    }

    @Test
    void doesNotTriggerWithoutALandEnteringThisTurn() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        resolveControllerEndStep();

        harness.assertNotOnBattlefield(player1, "Primo, the Indivisible");
    }

    @Test
    void opponentLandEntryDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        resolveControllerEndStep();

        harness.assertNotOnBattlefield(player1, "Primo, the Indivisible");
    }

    @Test
    void oneLandIsNotPrime() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        resolveControllerEndStep();

        harness.assertNotOnBattlefield(player1, "Primo, the Indivisible");
    }

    @Test
    void usesLandCountAtResolutionWhenItChangesToAnotherPrime() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Primo, the Indivisible")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Primo, the Indivisible");
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
