package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSkimmer.class, Forest.class})
class ManaSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to a player offers only that player's lands and locks the chosen land")
    void damagesPlayerAndLocksChosenLand() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent damagedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(damagedLand.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(damagedLand.getId()));

        assertThat(damagedLand.isTapped()).isTrue();
        assertThat(damagedLand.getSkipUntapCount()).isEqualTo(1);
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The chosen land stays tapped through its next untap step")
    void chosenLandStaysTappedThroughNextUntapStep() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent damagedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(damagedLand.getId()));

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        assertThat(damagedLand.isTapped()).isTrue();
        assertThat(damagedLand.getSkipUntapCount()).isZero();
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
