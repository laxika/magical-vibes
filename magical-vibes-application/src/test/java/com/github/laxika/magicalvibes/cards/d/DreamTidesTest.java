package com.github.laxika.magicalvibes.cards.d;

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

class DreamTidesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creatures stay tapped through the untap step")
    void creaturesStayTappedThroughUntap() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent wizard = addTapped(player1, new FugitiveWizard());
        Permanent bears = addTapped(player1, new GrizzlyBears());

        advanceToNextTurn(player2);

        assertThat(wizard.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {2} untaps the chosen nongreen creature")
    void payingTwoUntapsChosenNongreenCreature() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent wizard = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(wizard.getId()));

        assertThat(wizard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {4} untaps two chosen nongreen creatures")
    void payingFourUntapsTwoNongreenCreatures() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent wizardA = addTapped(player1, new FugitiveWizard());
        Permanent wizardB = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(wizardA.getId(), wizardB.getId()));

        assertThat(wizardA.isTapped()).isFalse();
        assertThat(wizardB.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With only {1} available, no creature can be untapped")
    void insufficientManaLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent wizard = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped green creature is not offered by the upkeep trigger")
    void tappedGreenCreatureIsNotOfferedByUpkeepTrigger() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent bears = addTapped(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the nongreen creature tapped")
    void choosingNoneLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new DreamTides());
        Permanent wizard = addTapped(player1, new FugitiveWizard());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(wizard.isTapped()).isTrue();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
