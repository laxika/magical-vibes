package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class GlitterfangTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the end step")
    void returnsItselfAtEndStep() {
        Glitterfang fang = new Glitterfang();
        harness.addToBattlefield(player1, fang);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glitterfang");
        assertThat(gd.playerHands.get(player1.getId())).contains(fang);
    }

    @Test
    @DisplayName("Triggers during an opponent's end step")
    void triggersDuringOpponentsEndStep() {
        Glitterfang fang = new Glitterfang();
        harness.addToBattlefield(player1, fang);

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glitterfang");
        assertThat(gd.playerHands.get(player1.getId())).contains(fang);
    }
}
