package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmogsteedRider.class, GrizzlyBears.class})
class SmogsteedRiderTest extends BaseCardTest {

    @Test
    void otherAttackingCreaturesGainFear() {
        Permanent rider = addCreatureReady(player1, new SmogsteedRider());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FEAR)).isFalse();
    }

    @Test
    void fearWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SmogsteedRider());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.FEAR)).isFalse();
    }
}
