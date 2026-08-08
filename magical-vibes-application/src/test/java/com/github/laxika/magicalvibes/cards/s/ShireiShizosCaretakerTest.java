package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShireiShizosCaretakerTest extends BaseCardTest {

    /** Player1 has Shirei out and bolts their own {@code victim}, resolving the death trigger. */
    private void shireiWatchesOwnCreatureDie(String victimName) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent victim = findPermanent(player1, victimName);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
    }

    /** Advances from the precombat main phase to the end step, firing the delayed return. */
    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }

    @Test
    @DisplayName("A power-1 creature that died returns to the battlefield at the next end step")
    void returnsPowerOneCreatureAtEndStep() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new FugitiveWizard());

        shireiWatchesOwnCreatureDie("Fugitive Wizard");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        harness.assertInGraveyard(player1, "Fugitive Wizard");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Declining the may leaves the dead creature in the graveyard")
    void declinedReturnLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new FugitiveWizard());

        shireiWatchesOwnCreatureDie("Fugitive Wizard");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("A creature with power greater than 1 does not trigger Shirei")
    void doesNotTriggerForPowerTwoCreature() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        shireiWatchesOwnCreatureDie("Grizzly Bears");

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nothing returns if Shirei has left the battlefield by the end step")
    void noReturnWhenShireiLeftTheBattlefield() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new FugitiveWizard());

        shireiWatchesOwnCreatureDie("Fugitive Wizard");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent shirei = findPermanent(player1, "Shirei, Shizo's Caretaker");
        gd.playerBattlefields.get(player1.getId()).remove(shirei);

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }
}
