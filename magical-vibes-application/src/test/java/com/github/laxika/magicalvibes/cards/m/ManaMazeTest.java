package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.g.GalinasKnight;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaMaze.class, GoblinSpy.class, GalinasKnight.class, ChromaticSphere.class})
class ManaMazeTest extends BaseCardTest {

    private void addManaMaze() {
        harness.addToBattlefield(player1, new ManaMaze());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("The first spell cast with Mana Maze on the battlefield is allowed")
    void allowsFirstSpellOfTurn() {
        addManaMaze();
        harness.castFromHand(player1, new GoblinSpy(), "{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell sharing the most recent spell's color cannot be cast")
    void rejectsSpellSharingMostRecentColor() {
        addManaMaze();
        harness.castFromHand(player1, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromHand(player1, new GoblinSpy(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell with a different color can be cast")
    void allowsSpellWithDifferentColor() {
        addManaMaze();
        harness.castFromHand(player1, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new GalinasKnight(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Colorless spells share no color and reset the restriction")
    void colorlessSpellSharesNoColor() {
        addManaMaze();
        harness.castFromHand(player1, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new ChromaticSphere(), "{1}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new GoblinSpy(), "{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restriction uses the most recent spell globally")
    void usesMostRecentSpellAcrossPlayers() {
        addManaMaze();
        harness.castFromHand(player1, new GalinasKnight(), "{W}{U}");
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GalinasKnight(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multicolored spells are restricted when they share any color")
    void rejectsMulticoloredSpellSharingAnyColor() {
        addManaMaze();
        harness.castFromHand(player1, new ManaMaze(), "{1}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromHand(player1, new GalinasKnight(), "{W}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The restriction applies to both players")
    void restrictsBothPlayers() {
        addManaMaze();
        harness.castFromHand(player1, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new GoblinSpy(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The restriction resets at the beginning of a new turn")
    void resetsAtBeginningOfNewTurn() {
        addManaMaze();
        harness.castFromHand(player1, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new GoblinSpy(), "{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    @DisplayName("Colors made colorless before announcement do not share a color")
    void ignoresColorsMadeColorlessBeforeCasting() {
        addManaMaze();
        harness.addToBattlefield(player1, new MycosynthLattice());

        harness.castFromHand(player1, new GoblinSpy(), "{R}");
        harness.passBothPriorities();

        assertThatCode(() -> harness.castFromHand(player1, new GoblinSpy(), "{R}"))
                .doesNotThrowAnyException();
        assertThat(gd.stack).hasSize(1);
    }
}
