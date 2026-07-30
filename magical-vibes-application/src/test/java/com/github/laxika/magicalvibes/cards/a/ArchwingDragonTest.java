package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ArchwingDragonTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances into END_STEP
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the end step")
    void returnsSelfToHandAtEndStep() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ArchwingDragon()));

        advanceToEndStep(player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Archwing Dragon");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Archwing Dragon"));
    }

    @Test
    @DisplayName("Triggers at every end step, including an opponent's")
    void triggersOnOpponentEndStep() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ArchwingDragon()));

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Archwing Dragon");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Archwing Dragon"));
    }
}
