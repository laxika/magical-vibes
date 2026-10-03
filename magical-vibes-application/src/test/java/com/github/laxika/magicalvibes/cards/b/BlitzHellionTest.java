package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlitzHellion.class})
class BlitzHellionTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Owner shuffles it into their library at the end step")
    void shufflesIntoOwnerLibraryAtEndStep() {
        harness.addToBattlefield(player1, new BlitzHellion());

        advanceToEndStep(player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Blitz Hellion");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blitz Hellion"));
    }

    @Test
    @DisplayName("Triggers at every end step, including an opponent's")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new BlitzHellion());

        advanceToEndStep(player2);

        // End-step trigger fires even though its controller is not the active player.
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        // Only the shuffle-into-library effect removes it, so leaving the battlefield proves it fired.
        harness.assertNotOnBattlefield(player1, "Blitz Hellion");
    }

    @Test
    @DisplayName("An opponent-controlled Hellion goes to its owner's library")
    void shufflesIntoOwnerLibraryUnderOpponentControl() {
        BlitzHellion card = new BlitzHellion();
        card.setOwnerId(player1.getId());
        Permanent hellion = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(hellion.getId(), player1.getId());

        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Blitz Hellion");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger does not shuffle a Hellion that has left the battlefield")
    void doesNotShuffleHellionThatLeftBattlefield() {
        BlitzHellion card = new BlitzHellion();
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, card);
        advanceToEndStep(player1);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, hellion));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Blitz Hellion");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning to the battlefield does not let the old trigger shuffle it")
    void oldTriggerDoesNotShuffleReturnedHellion() {
        BlitzHellion card = new BlitzHellion();
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, card);
        advanceToEndStep(player1);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, hellion));
        gd.playerHands.get(player1.getId()).remove(card);
        harness.enterBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blitz Hellion");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
