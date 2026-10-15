package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraudingBlightPriest.class, AngelOfMercy.class, WitchbaneOrb.class})
class MaraudingBlightPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when controller gains life")
    void eachOpponentLosesOneLifeWhenControllerGainsLife() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());

        int controllerStartingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerStartingLife + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());

        int opponentStartingLife = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife + 3);
    }

    @Test
    @DisplayName("Separate life-gain events each cause one life loss in the same turn")
    void triggersForEachSeparateLifeGainEvent() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());
        int controllerStartingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();
            assertThat(gd.getLife(player1.getId())).isEqualTo(controllerStartingLife + 3 * (i + 1));
            assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - (i + 1));
        }
    }

    @Test
    @DisplayName("Each Priest triggers independently for a single life-gain event")
    void multiplePriestsEachTriggerOnce() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());
        harness.addToBattlefield(player1, new MaraudingBlightPriest());
        int controllerStartingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerStartingLife + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 2);
    }

    @Test
    @DisplayName("A queued life-loss trigger resolves after its Priest leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        var priest = harness.addToBattlefieldAndReturn(player1, new MaraudingBlightPriest());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, priest));
        harness.assertInGraveyard(player1, "Marauding Blight-Priest");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 1);
    }

    @Test
    @DisplayName("An opponent with hexproof still loses life because the ability does not target")
    void opponentHexproofDoesNotPreventLifeLoss() {
        harness.addToBattlefield(player1, new MaraudingBlightPriest());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        int controllerStartingLife = gd.getLife(player1.getId());
        int opponentStartingLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerStartingLife + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentStartingLife - 1);
    }
}
