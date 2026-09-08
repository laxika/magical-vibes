package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DireFleetWarmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Dire Fleet Warmonger +2/+2 and trample")
    void sacrificingAnotherCreatureBoostsAndGrantsTrample() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("The ability does nothing when there is no other creature")
    void noOtherCreatureDoesNothing() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability triggers only during its controller's combat")
    void doesNotTriggerDuringOpponentCombat() {
        addCreatureReady(player1, new DireFleetWarmonger());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        Permanent warmonger = addCreatureReady(player1, new DireFleetWarmonger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warmonger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warmonger, Keyword.TRAMPLE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
