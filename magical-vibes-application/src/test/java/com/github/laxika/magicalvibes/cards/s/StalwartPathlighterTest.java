package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalwartPathlighter.class, GrizzlyBears.class, LlanowarElves.class})
class StalwartPathlighterTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void endTurn() {
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Coven gives your creatures indestructible at the beginning of combat")
    void covenGrantsIndestructibleToYourCreatures() {
        Permanent pathlighter = harness.addToBattlefieldAndReturn(player1, new StalwartPathlighter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pathlighter, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Coven does not trigger without three different powers")
    void covenRequiresThreeDifferentPowers() {
        Permanent pathlighter = harness.addToBattlefieldAndReturn(player1, new StalwartPathlighter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pathlighter, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent pathlighter = harness.addToBattlefieldAndReturn(player1, new StalwartPathlighter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, pathlighter, Keyword.INDESTRUCTIBLE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, pathlighter, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
