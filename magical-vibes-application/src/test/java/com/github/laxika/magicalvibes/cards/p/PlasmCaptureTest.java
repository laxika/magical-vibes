package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlasmCaptureTest extends BaseCardTest {

    /** Player1 casts Grizzly Bears (mana value 2); Player2 holds Plasm Capture to counter it. */
    private GrizzlyBears prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PlasmCapture()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        return bears;
    }

    private void counterBears(GrizzlyBears bears) {
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counters the spell and adds mana in any combination of colors at the caster's next first main phase")
    void countersAndAddsManaAtNextFirstMainPhase() {
        GrizzlyBears bears = prepareCounterTarget();

        counterBears(bears);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        AddManaAtNextMainPhase reward = gd.getDelayedActions(AddManaAtNextMainPhase.class).getFirst();
        assertThat(reward.controllerId()).isEqualTo(player2.getId());
        assertThat(reward.amount()).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();

        // Mandatory: the delayed ability resolves straight into the color picks, one per mana.
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        PendingInteraction.ColorChoice choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player2, "RED");
        harness.handleListChoice(player2, "WHITE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The delayed mana waits for a first main phase and does not fire on a postcombat main")
    void doesNotFireOnPostcombatMain() {
        GrizzlyBears bears = prepareCounterTarget();

        counterBears(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
    }
}
