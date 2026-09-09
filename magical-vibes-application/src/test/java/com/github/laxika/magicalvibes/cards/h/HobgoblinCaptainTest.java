package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HobgoblinCaptain.class, CentaurCourser.class, GrizzlyBears.class})
class HobgoblinCaptainTest extends BaseCardTest {

    @Test
    void gainsFirstStrikeWhenAttackingCreaturesHaveTotalPowerAtLeastSix() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new CentaurCourser());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doesNotGainFirstStrikeWhenAttackingCreaturesHaveTotalPowerLessThanSix() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new CentaurCourser());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isFalse();
    }
}
