package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GloriousEnforcer.class)
class GloriousEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of each combat, gains double strike when its controller has more life")
    void gainsDoubleStrikeWhenControllerHasMoreLife() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain double strike when its controller does not have more life")
    void doesNotGainDoubleStrikeWhenControllerDoesNotHaveMoreLife() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The double strike grant wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Triggers during an opponent's combat")
    void triggersDuringOpponentsCombat() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player2);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
