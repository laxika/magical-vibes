package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RibbonSnakeTest extends BaseCardTest {

    @Test
    void anyPlayerMayPayToRemoveFlying() {
        Permanent snake = harness.addToBattlefieldAndReturn(player1, new RibbonSnake());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snake, Keyword.FLYING)).isFalse();
    }

    @Test
    void lostFlyingReturnsAtEndOfTurn() {
        Permanent snake = harness.addToBattlefieldAndReturn(player1, new RibbonSnake());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snake, Keyword.FLYING)).isTrue();
    }
}
