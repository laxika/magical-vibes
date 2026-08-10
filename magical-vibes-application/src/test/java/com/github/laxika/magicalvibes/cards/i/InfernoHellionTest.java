package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernoHellionTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not trigger if it did not attack or block this turn")
    void doesNotTriggerWithoutAttackingOrBlocking() {
        addCreatureReady(player1, new InfernoHellion());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Inferno Hellion");
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library after attacking")
    void shufflesAfterAttacking() {
        addCreatureReady(player1, new InfernoHellion());

        declareAttackers(player1, List.of(0));
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Inferno Hellion");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Hellion"));
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library after blocking")
    void shufflesAfterBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new InfernoHellion());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Inferno Hellion");
        assertThat(gameLogContains("Inferno Hellion is shuffled into its owner's library.")).isTrue();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Hellion"));
    }
}
