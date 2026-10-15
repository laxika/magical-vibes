package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainbowVale.class, RuinsOfTrokair.class})
class RainbowValeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds the chosen color and gives the land to an opponent at the next end step")
    void tapsForManaThenChangesControllerAtNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RainbowVale());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Tapping another land does not schedule a control change")
    void tappingAnotherLandDoesNotChangeControl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent rainbowVale = harness.addToBattlefieldAndReturn(player1, new RainbowVale());
        harness.addToBattlefield(player1, new RuinsOfTrokair());

        harness.activateAbility(player1, 1, null, null);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rainbowVale);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rainbowVale);
    }

    @Test
    @DisplayName("Tapping during the end step changes control at the following end step")
    void tappingDuringEndStepWaitsForFollowingEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RainbowVale());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Changing control preserves the land's tapped state")
    void controlChangePreservesTappedState() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RainbowVale());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.CLEANUP, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mana ability creates no immediate stack entry")
    void manaAbilityRegistersControlChangeWithoutUsingStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RainbowVale());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.CLEANUP, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("An opponent who activates Rainbow Vale gives it back at the next end step")
    void opponentActivationTransfersLandBack() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RainbowVale());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLACK");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.CLEANUP, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
    }
}
