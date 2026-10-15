package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchHunt.class, RenewedFaith.class})
class WitchHuntTest extends BaseCardTest {

    @Test
    void dealsFourDamageToItsControllerOnTheirUpkeep() {
        harness.addToBattlefield(player1, new WitchHunt());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void preventsLifeGainForBothPlayers() {
        harness.addToBattlefield(player1, new WitchHunt());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void preventsItsControllerFromGainingLife() {
        harness.addToBattlefield(player1, new WitchHunt());
        harness.castFromHand(player1, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void canBeCastWithoutChoosingATarget() {
        harness.castFromHand(player1, new WitchHunt(), "{4}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Witch Hunt");
    }

    @Test
    void doesNotDealDamageDuringAnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WitchHunt());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTransferDuringAnOpponentsEndStep() {
        harness.addToBattlefield(player1, new WitchHunt());

        advanceToEndStep(player2);

        harness.assertOnBattlefield(player1, "Witch Hunt");
        harness.assertNotOnBattlefield(player2, "Witch Hunt");
    }

    @Test
    void damagesTheNewControllerOnTheirUpkeep() {
        harness.addToBattlefield(player1, new WitchHunt());
        advanceToEndStep(player1);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void upkeepDamageStillResolvesAfterTheSourceLeaves() {
        var witchHunt = harness.addToBattlefieldAndReturn(player1, new WitchHunt());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, witchHunt));
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifeGainResumesAfterWitchHuntLeaves() {
        var witchHunt = harness.addToBattlefieldAndReturn(player1, new WitchHunt());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, witchHunt));

        harness.castFromHand(player1, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    @Test
    void endStepTriggerCannotTransferANewObjectAfterTheSourceLeaves() {
        var witchHunt = harness.addToBattlefieldAndReturn(player1, new WitchHunt());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, witchHunt));
        harness.addToBattlefield(player1, new WitchHunt());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Witch Hunt");
        harness.assertNotOnBattlefield(player2, "Witch Hunt");
    }

    @Test
    void transfersControlToTheOnlyOpponentAtItsControllerEndStep() {
        harness.addToBattlefield(player1, new WitchHunt());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Witch Hunt")).isEmpty();
        assertThat(findPermanents(player2, "Witch Hunt")).hasSize(1);

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Witch Hunt")).hasSize(1);
        assertThat(findPermanents(player2, "Witch Hunt")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
