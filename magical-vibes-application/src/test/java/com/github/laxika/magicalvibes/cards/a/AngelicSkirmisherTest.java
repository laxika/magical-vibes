package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, first strike is granted to all creatures you control")
    void grantsFirstStrikeToOwnCreatures() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleListChoice(player1, "First strike");
        harness.passBothPriorities();

        assertThat(skirmisher.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opposingBear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Choosing vigilance grants vigilance until end of turn")
    void grantsVigilanceUntilEndOfTurn() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleListChoice(player1, "Vigilance");
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.VIGILANCE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Triggers during an opponent's combat and can grant lifelink")
    void triggersDuringOpponentsCombat() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player2);
        harness.handleListChoice(player1, "Lifelink");
        harness.passBothPriorities();

        assertThat(skirmisher.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(opposingBear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
