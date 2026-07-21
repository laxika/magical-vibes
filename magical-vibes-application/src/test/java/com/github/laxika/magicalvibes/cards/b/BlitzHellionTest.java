package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class BlitzHellionTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances into END_STEP
    }

    @Test
    @DisplayName("Owner shuffles it into their library at the end step")
    void shufflesIntoOwnerLibraryAtEndStep() {
        Permanent hellion = new Permanent(new BlitzHellion());
        gd.playerBattlefields.get(player1.getId()).add(hellion);

        advanceToEndStep(player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blitz Hellion"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blitz Hellion"));
    }

    @Test
    @DisplayName("Triggers at every end step, including an opponent's")
    void triggersOnOpponentEndStep() {
        Permanent hellion = new Permanent(new BlitzHellion());
        gd.playerBattlefields.get(player1.getId()).add(hellion);

        advanceToEndStep(player2);

        // End-step trigger fires even though its controller is not the active player.
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        // Only the shuffle-into-library effect removes it, so leaving the battlefield proves it fired.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blitz Hellion"));
    }
}
