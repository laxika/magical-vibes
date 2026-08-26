package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidewaterMinion.class, GrizzlyBears.class})
class TidewaterMinionTest extends BaseCardTest {

    @Test
    void losesDefenderUntilEndOfTurn() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(minion.hasKeyword(Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(minion.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    void untapsTargetPermanent() {
        Permanent minion = addCreatureReady(player1, new TidewaterMinion());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(minion.isTapped()).isTrue();
    }
}
