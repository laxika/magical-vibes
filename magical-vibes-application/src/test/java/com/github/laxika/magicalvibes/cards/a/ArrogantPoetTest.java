package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
