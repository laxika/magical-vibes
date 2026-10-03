package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchwingDragon.class})
class ArchwingDragonTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the end step")
    void returnsSelfToHandAtEndStep() {
        harness.addToBattlefield(player1, new ArchwingDragon());

        advanceToEndStep(player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Archwing Dragon");
        harness.assertInHand(player1, "Archwing Dragon");
    }

    @Test
    @DisplayName("Triggers at every end step, including an opponent's")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ArchwingDragon());

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Archwing Dragon");
        harness.assertInHand(player1, "Archwing Dragon");
    }

    @Test
    @DisplayName("Each Dragon returns only itself when its trigger resolves")
    void multipleDragonsReturnIndependently() {
        ArchwingDragon first = new ArchwingDragon();
        ArchwingDragon second = new ArchwingDragon();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Archwing Dragon");
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("A stolen Dragon returns to its owner rather than its controller")
    void stolenDragonReturnsToOwner() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ArchwingDragon());
        gd.stolenCreatures.put(dragon.getId(), player1.getId());

        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Archwing Dragon");
        harness.assertInHand(player1, "Archwing Dragon");
        harness.assertNotInHand(player2, "Archwing Dragon");
    }

    @Test
    @DisplayName("A Dragon entering after the end step begins waits until the next end step")
    void enteringDuringEndStepDoesNotTriggerImmediately() {
        advanceToEndStep(player1);
        harness.enterBattlefieldAndReturn(player1, new ArchwingDragon());

        assertThat(gd.stack).isEmpty();
        harness.passUntil(TurnStep.CLEANUP);

        harness.assertOnBattlefield(player1, "Archwing Dragon");
        harness.assertNotInHand(player1, "Archwing Dragon");

        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Archwing Dragon");
        harness.assertInHand(player1, "Archwing Dragon");
    }
}
