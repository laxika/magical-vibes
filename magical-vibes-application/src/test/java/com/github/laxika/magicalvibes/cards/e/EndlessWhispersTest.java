package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessWhispers.class, DrossCrocodile.class, DevourInShadow.class})
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
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
