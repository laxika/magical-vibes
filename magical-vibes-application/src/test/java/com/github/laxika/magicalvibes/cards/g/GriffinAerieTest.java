package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GriffinAerieTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 white flying Griffin token after gaining 3 life")
    void createsGriffinTokenAtThreeLifeGained() {
        harness.addToBattlefield(player1, new GriffinAerie());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        var griffins = findPermanents(player1, "Griffin");
        assertThat(griffins).hasSize(1);
        assertThat(griffins).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Does not create a token below the life-gain threshold")
    void noTokenBelowThreshold() {
        harness.addToBattlefield(player1, new GriffinAerie());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Griffin")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        harness.addToBattlefield(player1, new GriffinAerie());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Griffin")).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
