package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummitIntimidator.class, GrizzlyBears.class})
class SummitIntimidatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the targeted creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }
}
