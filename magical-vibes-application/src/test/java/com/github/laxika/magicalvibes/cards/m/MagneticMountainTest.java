package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagneticMountainTest extends BaseCardTest {

    // ===== Static: blue creatures don't untap during their controllers' untap steps =====

    @Test
    @DisplayName("A tapped blue creature stays tapped through the untap step while a non-blue one untaps")
    void blueCreatureStaysTappedWhileGreenUntaps() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent wizard = addTapped(player1, new FugitiveWizard()); // Blue 1/1
        Permanent bears = addTapped(player1, new GrizzlyBears());     // Green 2/2

        advanceToNextTurn(player2); // roll into player1's untap step

        assertThat(wizard.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Upkeep: pay {4} per chosen blue creature to untap =====

    @Test
    @DisplayName("Paying {4} untaps the chosen blue creature")
    void payingFourUntapsChosenBlueCreature() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent wizard = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities(); // resolve the trigger -> begins the multi-permanent choice
        harness.handleMultiplePermanentsChosen(player1, List.of(wizard.getId()));

        assertThat(wizard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {8} untaps two chosen blue creatures")
    void payingEightUntapsTwoBlueCreatures() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent wizardA = addTapped(player1, new FugitiveWizard());
        Permanent wizardB = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(wizardA.getId(), wizardB.getId()));

        assertThat(wizardA.isTapped()).isFalse();
        assertThat(wizardB.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With only {3} available, no creature can be untapped (cost of {4} not met)")
    void insufficientManaLeavesBlueCreatureTapped() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent wizard = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities(); // trigger resolves as a no-op — can't afford any creature

        assertThat(wizard.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the blue creature tapped")
    void choosingNoneLeavesBlueCreatureTapped() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent wizard = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(wizard.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP, fires the each-upkeep trigger
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn runs the untap step)
    }
}
