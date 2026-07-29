package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerfolkRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Merfolk Raiders phases out during its controller's untap step and phases back in the next one")
    void phasesOutAndInOnControllersUntapSteps() {
        Permanent raiders = addCreatureReady(player1, new MerfolkRaiders());

        advanceTurn(); // player2's untap step — nothing happens to player1's permanents
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(raiders);

        advanceTurn(); // player1's untap step — Merfolk Raiders phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(raiders);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(raiders);

        advanceTurn(); // player2's untap step — still phased out
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(raiders);

        advanceTurn(); // player1's untap step — phases back in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(raiders);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
