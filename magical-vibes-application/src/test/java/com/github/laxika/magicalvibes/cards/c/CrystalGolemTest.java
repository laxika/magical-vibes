package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrystalGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Crystal Golem phases out at the beginning of its controller's end step")
    void phasesOutAtControllerEndStep() {
        Permanent golem = addGolem();

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Only the Golem phases out — the trigger phases out no other permanent")
    void phasesOutOnlyItself() {
        Permanent golem = addGolem();
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve the Golem's trigger only

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentPermanent);
    }

    @Test
    @DisplayName("Crystal Golem does not phase out during the opponent's end step")
    void doesNotPhaseOutAtOpponentEndStep() {
        Permanent golem = addGolem();

        advanceToEndStep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Crystal Golem phases back in during its controller's next untap step")
    void phasesBackInNextUntapStep() {
        Permanent golem = addGolem();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(golem);
    }

    private Permanent addGolem() {
        Permanent perm = new Permanent(new CrystalGolem());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
