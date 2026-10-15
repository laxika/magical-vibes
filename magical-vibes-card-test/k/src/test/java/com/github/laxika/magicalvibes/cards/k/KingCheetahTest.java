package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KingCheetah.class)
class KingCheetahTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows King Cheetah to be cast during an opponent's turn")
    void flashAllowsCastingDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new KingCheetah(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "King Cheetah");
    }

    @Test
    @DisplayName("Flash allows King Cheetah to be cast during an end step")
    void flashAllowsCastingDuringEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new KingCheetah(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "King Cheetah");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "King Cheetah");
    }

    @Test
    @DisplayName("Flash allows King Cheetah to resolve before the spell it responds to")
    void flashAllowsRespondingToCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new KingCheetah(), "{3}{G}");

        harness.castFromHand(player1, new KingCheetah(), "{3}{G}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "King Cheetah");
        harness.assertNotOnBattlefield(player2, "King Cheetah");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "King Cheetah");
        assertThat(gd.stack).isEmpty();
    }
}
