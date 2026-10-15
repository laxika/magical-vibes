package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArrogantPoet.class})
class ArrogantPoetTest extends BaseCardTest {

    @Test
    void mayPayLifeToGainFlying() {
        Permanent poet = addCreatureReady(player1, new ArrogantPoet());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isTrue();
    }

    @Test
    void decliningLifePaymentDoesNothing() {
        Permanent poet = addCreatureReady(player1, new ArrogantPoet());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingWearsOffAtEndOfTurn() {
        Permanent poet = addCreatureReady(player1, new ArrogantPoet());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotPayTwoLifeWithOnlyOneLife() {
        Permanent poet = addCreatureReady(player1, new ArrogantPoet());
        harness.setLife(player1, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 1);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isFalse();
    }

    @Test
    void onlyAttackingPoetGainsFlying() {
        Permanent idlePoet = addCreatureReady(player1, new ArrogantPoet());
        Permanent attackingPoet = addCreatureReady(player1, new ArrogantPoet());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gqs.hasKeyword(gd, attackingPoet, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, idlePoet, Keyword.FLYING)).isFalse();
    }

    @Test
    void defendingPlayersPoetDoesNotTriggerAndAttackingControllerPays() {
        Permanent defendingPoet = addCreatureReady(player1, new ArrogantPoet());
        Permanent attackingPoet = addCreatureReady(player2, new ArrogantPoet());
        int defendingLifeBefore = gd.getLife(player1.getId());
        int attackingLifeBefore = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(gd.currentStep, () -> harness.handleMayAbilityChosen(player2, true));

        harness.assertLife(player2, attackingLifeBefore - 2);
        harness.assertLife(player1, defendingLifeBefore);
        assertThat(gqs.hasKeyword(gd, attackingPoet, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, defendingPoet, Keyword.FLYING)).isFalse();
    }
}
