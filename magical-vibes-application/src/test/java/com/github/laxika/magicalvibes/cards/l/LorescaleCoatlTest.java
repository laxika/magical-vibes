package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LorescaleCoatlTest extends BaseCardTest {

    @Test
    @DisplayName("Draw step draw puts a +1/+1 counter on Lorescale Coatl")
    void triggersOnDrawStepDraw() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Drawing multiple cards from a spell adds one counter per card drawn")
    void triggersOncePerCardDrawn() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Counsel of the Soratami draws 2 cards.
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel of the Soratami (draws 2)
        harness.passBothPriorities(); // resolve first Coatl trigger
        harness.passBothPriorities(); // resolve second Coatl trigger

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent drawing a card does not trigger Lorescale Coatl")
    void doesNotTriggerOnOpponentDraw() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());

        advanceToDraw(player2);

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }
}
