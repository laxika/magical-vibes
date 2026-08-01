package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BreezekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Breezekeeper phases out during its controller's untap step and phases back in the next one")
    void phasesOutAndInOnControllersUntapSteps() {
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());

        advanceTurn(); // player2's untap step — nothing happens to player1's permanents
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);

        advanceTurn(); // player1's untap step — Breezekeeper phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keeper);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);

        advanceTurn(); // player2's untap step — still phased out
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(keeper);

        advanceTurn(); // player1's untap step — phases back in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
