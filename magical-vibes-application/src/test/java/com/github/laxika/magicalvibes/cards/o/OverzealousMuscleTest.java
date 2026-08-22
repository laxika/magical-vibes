package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverzealousMuscle.class, Shock.class})
class OverzealousMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible when its controller commits a crime during their turn")
    void gainsIndestructibleAfterCrimeDuringOwnTurn() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());

        castShockAt(player2.getId());

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when its controller commits a crime during an opponent's turn")
    void doesNotTriggerDuringOpponentTurn() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castShockAt(player2.getId());

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at the end of the turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        castShockAt(player2.getId());
        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castShockAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
