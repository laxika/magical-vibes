package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoraugFuryOfAkoum.class, GrizzlyBears.class, Mountain.class})
class MoraugFuryOfAkoumTest extends BaseCardTest {

    @Test
    void boostsEachCreatureByItsNumberOfAttacksThisTurn() {
        Permanent moraug = addCreatureReady(player1, new MoraugFuryOfAkoum());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, moraug)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);

        declareAttackers(List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, moraug)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);

        resolveCombat();
        moraug.untap();
        bear.untap();
        declareAttackers(List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, moraug)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
    }

    @Test
    void landfallDuringMainPhaseAddsCombatAndUntapsControlledCreatures() {
        addCreatureReady(player1, new MoraugFuryOfAkoum());
        Permanent tappedBear = addCreatureReady(player1, new GrizzlyBears());
        tappedBear.tap();
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        opposingBear.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(tappedBear.isTapped()).isTrue();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(tappedBear.isTapped()).isFalse();
        assertThat(opposingBear.isTapped()).isTrue();
    }

    @Test
    void precombatLandfallCombatIsFollowedDirectlyByRegularCombat() {
        addCreatureReady(player1, new MoraugFuryOfAkoum());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void postcombatLandfallDoesNotAddAMainPhase() {
        Permanent moraug = addCreatureReady(player1, new MoraugFuryOfAkoum());
        moraug.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        assertThat(moraug.isTapped()).isFalse();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    void multipleLandfallsAddConsecutiveCombats() {
        Permanent moraug = addCreatureReady(player1, new MoraugFuryOfAkoum());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.enterBattlefieldAndReturn(player1, new Mountain());
        resolveAllTriggers();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        moraug.tap();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(moraug.isTapped()).isTrue();
        resolveAllTriggers();
        assertThat(moraug.isTapped()).isFalse();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    void landEnteringDuringCombatDoesNotTrigger() {
        addCreatureReady(player1, new MoraugFuryOfAkoum());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.enterBattlefieldAndReturn(player1, new Mountain());

        assertThat(gd.stack).isEmpty();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void landEnteringDuringOpponentsMainPhaseDoesNotTrigger() {
        addCreatureReady(player1, new MoraugFuryOfAkoum());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new Mountain());

        assertThat(gd.stack).isEmpty();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    void opponentsLandDoesNotTriggerDuringControllersMainPhase() {
        addCreatureReady(player1, new MoraugFuryOfAkoum());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player2, new Mountain());

        assertThat(gd.stack).isEmpty();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    void countsAttacksBeforeMoraugEnteredAndStopsBoostingWhenItLeaves() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        Permanent moraug = harness.enterBattlefieldAndReturn(player1, new MoraugFuryOfAkoum());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(moraug);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }
}
