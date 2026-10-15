package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServantOfVolrath.class, LowlandGiant.class, Forest.class})
class ServantOfVolrathTest extends BaseCardTest {

    @Test
    @DisplayName("Dying forces the controller to sacrifice their only other creature")
    void diesSacrificesOnlyOtherCreature() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new LowlandGiant());

        destroyServant();
        resolveTrigger();

        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("Controller chooses which creature to sacrifice when several are available")
    void controllerChoosesAmongMultipleCreatures() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new LowlandGiant());
        harness.addToBattlefield(player1, new LowlandGiant());

        destroyServant();
        resolveTrigger();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Lowland Giant"));

        assertThat(countPermanents(player1, "Lowland Giant")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures are untouched by the sacrifice trigger")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player2, new LowlandGiant());

        destroyServant();
        resolveTrigger();

        harness.assertOnBattlefield(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("No creature remains when the trigger resolves")
    void noCreatureToSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new ServantOfVolrath());

        destroyServant();
        resolveTrigger();

        harness.assertInGraveyard(player1, "Servant of Volrath");
    }

    @Test
    @DisplayName("Bouncing this creature also triggers the sacrifice")
    void bounceTriggersSacrifice() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new LowlandGiant());

        Permanent servant = findPermanent(player1, "Servant of Volrath");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, servant));
        resolveTrigger();

        harness.assertNotOnBattlefield(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("The trigger sacrifices a creature, not a noncreature permanent")
    void noncreaturePermanentIsNotSacrificed() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new Forest());

        destroyServant();
        resolveTrigger();

        harness.assertOnBattlefield(player1, "Forest");
    }

    private void destroyServant() {
        Permanent servant = findPermanent(player1, "Servant of Volrath");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servant));
    }

    @Test
    @DisplayName("Exiling this creature also triggers the sacrifice")
    void exileTriggersSacrifice() {
        harness.addToBattlefield(player1, new ServantOfVolrath());
        harness.addToBattlefield(player1, new LowlandGiant());

        Permanent servant = findPermanent(player1, "Servant of Volrath");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToExile(gd, servant));
        resolveTrigger();

        harness.assertNotOnBattlefield(player1, "Servant of Volrath");
        harness.assertNotInGraveyard(player1, "Servant of Volrath");
        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("A creature entering after the trigger is created can be sacrificed")
    void creatureEnteringBeforeResolutionIsSacrificed() {
        harness.addToBattlefield(player1, new ServantOfVolrath());

        destroyServant();
        harness.addToBattlefield(player1, new LowlandGiant());
        resolveTrigger();

        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("A Servant controlled by the opponent makes that opponent sacrifice")
    void opponentsServantMakesOpponentSacrifice() {
        harness.addToBattlefield(player2, new ServantOfVolrath());
        harness.addToBattlefield(player2, new LowlandGiant());
        harness.addToBattlefield(player1, new LowlandGiant());

        Permanent servant = findPermanent(player2, "Servant of Volrath");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servant));
        resolveTrigger();

        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.assertNotOnBattlefield(player2, "Lowland Giant");
        harness.assertInGraveyard(player2, "Lowland Giant");
    }

    private void resolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
