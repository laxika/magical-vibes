package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystallineResonance.class, Censor.class, GrizzlyBears.class})
class CrystallineResonanceTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling offers another permanent as a copy target")
    void cyclingQueuesAnotherPermanentTarget() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cycleCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(resonance.getId());
    }

    @Test
    @DisplayName("Accepting the trigger copies a permanent until the controller's next turn")
    void copiesUntilControllersNextTurn() {
        Permanent resonance = addResonance();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveCopy(bears);

        assertThat(gqs.isCreature(gd, resonance)).isTrue();
        assertThat(gqs.getEffectivePower(gd, resonance)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, resonance)).isEqualTo(2);

        endTurn(player1);
        assertThat(gqs.isCreature(gd, resonance)).isTrue();

        endTurn(player2);
        assertThat(gqs.isCreature(gd, resonance)).isFalse();
    }

    @Test
    @DisplayName("The copied permanent retains Crystalline Resonance's cycling trigger")
    void copiedPermanentRetainsCyclingTrigger() {
        Permanent resonance = addResonance();
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveCopy(firstTarget);
        cycleCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstTarget.getId(), secondTarget.getId())
                .doesNotContain(resonance.getId());
    }

    private Permanent addResonance() {
        return harness.addToBattlefieldAndReturn(player1, new CrystallineResonance());
    }

    private void cycleCard() {
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
    }

    private void resolveCopy(Permanent target) {
        cycleCard();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
