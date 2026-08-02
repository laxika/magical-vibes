package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServantOfVolrathTest extends BaseCardTest {

    @Test
    @DisplayName("Dying forces the controller to sacrifice their only other creature")
    void diesSacrificesOnlyOtherCreature() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyServant();
        resolveTrigger();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Controller chooses which creature to sacrifice when several are available")
    void controllerChoosesAmongMultipleCreatures() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyServant();
        resolveTrigger();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures are untouched by the sacrifice trigger")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyServant();
        resolveTrigger();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bouncing this creature also triggers the sacrifice")
    void bounceTriggersSacrifice() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent servant = findPermanent(player1, "Servant of Volrath");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, servant));
        resolveTrigger();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void destroyServant() {
        Permanent servant = findPermanent(player1, "Servant of Volrath");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servant));
    }

    private void resolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
