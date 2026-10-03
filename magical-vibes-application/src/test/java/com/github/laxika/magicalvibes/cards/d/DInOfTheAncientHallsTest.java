package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.StaunchShieldmate;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DInOfTheAncientHalls.class, StaunchShieldmate.class})
class DInOfTheAncientHallsTest extends BaseCardTest {

    @Test
    void attackingDealsDamageEqualToDwarvesYouControlToEachOpponent() {
        addCreatureReady(player1, new DInOfTheAncientHalls());
        addCreatureReady(player1, new StaunchShieldmate());
        addCreatureReady(player2, new StaunchShieldmate());

        int lifeBefore = gd.getLife(player2.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void countsDwarvesAtResolution() {
        addCreatureReady(player1, new DInOfTheAncientHalls());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        addCreatureReady(player1, new StaunchShieldmate());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    void newlyArrivedDwarfIncreasesAttackTriggerDamageBeforeCombat() {
        addCreatureReady(player1, new DInOfTheAncientHalls());
        int opponentLife = gd.getLife(player2.getId());
        int controllerLife = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            addCreatureReady(player1, new StaunchShieldmate());
            resolveAllTriggers();
        });

        harness.assertLife(player2, opponentLife - 2);
        harness.assertLife(player1, controllerLife);
    }

    @Test
    void sourceLeavingBeforeResolutionDealsZeroWithNoRemainingDwarves() {
        var dain = addCreatureReady(player1, new DInOfTheAncientHalls());
        int opponentLife = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(dain);
            harness.setGraveyard(player1, List.of(dain.getCard()));
            resolveAllTriggers();
        });

        harness.assertLife(player2, opponentLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentControlledAttackCountsTheirDwarvesAndDamagesTheirOpponent() {
        addCreatureReady(player2, new DInOfTheAncientHalls());
        addCreatureReady(player2, new StaunchShieldmate());
        addCreatureReady(player1, new StaunchShieldmate());
        int opponentLife = gd.getLife(player1.getId());
        int controllerLife = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        harness.assertLife(player1, opponentLife - 2);
        harness.assertLife(player2, controllerLife);
    }
}
