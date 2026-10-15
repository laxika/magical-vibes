package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Crystacean.class})
class CrystaceanTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows casting Crystacean during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Crystacean(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Flash allows responding to a creature spell during an opponent's end step")
    void canRespondDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Crystacean(), "{3}{U}");

        harness.castFromHand(player1, new Crystacean(), "{3}{U}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Crystacean");
        harness.assertNotOnBattlefield(player2, "Crystacean");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Crystacean");
    }
}
