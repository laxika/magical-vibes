package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessWhispers.class, DrossCrocodile.class, DevourInShadow.class,
        BeaconOfUnrest.class, Opalescence.class})
class EndlessWhispersTest extends BaseCardTest {

    @Test
    @DisplayName("A dying creature returns under a chosen opponent's control at the next end step")
    void returnsDyingCreatureUnderChosenOpponentsControl() {
        harness.addToBattlefield(player1, new EndlessWhispers());
        harness.addToBattlefield(player1, new DrossCrocodile());

        destroyCreature(player1, player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class))
                .singleElement()
                .extracting(DelayedGraveyardToBattlefieldUnderControl::controllerId)
                .isEqualTo(player2.getId());
        harness.assertInGraveyard(player1, "Dross Crocodile");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Dross Crocodile");
        harness.assertNotInGraveyard(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("The dying creature's controller chooses the opponent who gets it")
    void triggerIsControlledByDyingCreaturesController() {
        harness.addToBattlefield(player1, new EndlessWhispers());
        harness.addToBattlefield(player2, new DrossCrocodile());

        destroyCreature(player1, player2);

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class))
                .singleElement()
                .extracting(DelayedGraveyardToBattlefieldUnderControl::controllerId)
                .isEqualTo(player1.getId());

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Dross Crocodile");
        harness.assertNotInGraveyard(player2, "Dross Crocodile");
    }

    @Test
    @DisplayName("A dying creature does not return if its card leaves the graveyard first")
    void doesNotReturnIfCardLeavesGraveyardBeforeEndStep() {
        harness.addToBattlefield(player1, new EndlessWhispers());
        harness.addToBattlefield(player1, new DrossCrocodile());

        destroyCreature(player1, player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of());
        advanceToEndStep();

        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
    }

    @Test
    @DisplayName("The end-step return uses the stack and is controlled by the dying creature's controller")
    void endStepReturnWaitsForPriorityAndRetainsTriggerController() {
        harness.addToBattlefield(player1, new EndlessWhispers());
        harness.addToBattlefield(player1, new DrossCrocodile());

        destroyCreature(player1, player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Dross Crocodile");
        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dross Crocodile");
    }

    @Test
    @DisplayName("An earlier death's return cannot find the card after it is reanimated and dies again")
    void earlierReturnDoesNotAffectNewGraveyardObject() {
        harness.addToBattlefield(player1, new EndlessWhispers());
        var crocodile = new DrossCrocodile();
        harness.addToBattlefield(player1, crocodile);

        destroyCreature(player1, player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BeaconOfUnrest()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player2, 0, 0, crocodile.getId());
        harness.assertOnBattlefield(player2, "Dross Crocodile");

        destroyCreature(player1, player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Dross Crocodile");
        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        harness.assertNotInGraveyard(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("Endless Whispers grants its own death ability when it is animated")
    void animatedEndlessWhispersHasItsOwnDeathTrigger() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new EndlessWhispers());
        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Endless Whispers"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Endless Whispers");
        harness.assertNotInGraveyard(player1, "Endless Whispers");
    }

    private void destroyCreature(Player spellCaster, Player creatureController) {
        harness.forceActivePlayer(spellCaster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(spellCaster, List.of(new DevourInShadow()));
        harness.addMana(spellCaster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(spellCaster, 0,
                harness.getPermanentId(creatureController, "Dross Crocodile"));
    }

    private void advanceToEndStep() {
        harness.passUntil(TurnStep.END_STEP);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
