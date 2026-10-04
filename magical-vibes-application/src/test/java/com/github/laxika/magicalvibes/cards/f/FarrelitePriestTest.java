package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FarrelitePriest.class})
class FarrelitePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability adds {W}")
    void activatingAddsWhiteMana() {
        addReadyFarrelitePriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        addReadyFarrelitePriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Farrelite Priest");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four or more times")
    void sacrificedWhenActivatedFourOrMoreTimes() {
        addReadyFarrelitePriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Farrelite Priest");
        harness.assertInGraveyard(player1, "Farrelite Priest");
    }

    @Test
    @DisplayName("Every activation from the fourth onward creates a sacrifice trigger")
    void everyActivationFromFourthOnwardCreatesSacrificeTrigger() {
        addReadyFarrelitePriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Farrelite Priest");
        harness.assertInGraveyard(player1, "Farrelite Priest");
    }

    @Test
    @DisplayName("Activating four times during the end step waits for the next end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        addReadyFarrelitePriest(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Farrelite Priest");
        harness.assertInGraveyard(player1, "Farrelite Priest");
    }

    @Test
    @DisplayName("Activation count resets on the next player's turn")
    void activationCountResetsEachTurn() {
        addReadyFarrelitePriest(player1);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Farrelite Priest");
    }

    @Test
    @DisplayName("Two Priests count their activations independently")
    void activationCountsAreIndependentForEachPriest() {
        Permanent first = addReadyFarrelitePriest(player1);
        Permanent second = addReadyFarrelitePriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.activateAbility(player1, 1, null, null);
        }
        harness.activateAbility(player1, 0, null, null);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second).doesNotContain(first);
        harness.assertInGraveyard(player1, "Farrelite Priest");
    }

    @Test
    @DisplayName("A tapped summoning-sick Priest can filter mana immediately")
    void tappedSummoningSickPriestCanActivateManaAbility() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new FarrelitePriest());
        priest.setSummoningSick(true);
        priest.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(priest.isTapped()).isTrue();
    }
    private Permanent addReadyFarrelitePriest(Player player) {
        return addCreatureReady(player, new FarrelitePriest());
    }
}
