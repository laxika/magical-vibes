package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StensiaUprising.class, GrizzlyBears.class})
class StensiaUprisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Human token at the beginning of the controller's end step")
    void createsHumanTokenAtEndStep() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());

        resolveEndStepTrigger(player1);

        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("At exactly thirteen permanents, may sacrifice Stensia Uprising to deal 7 damage")
    void sacrificesAtExactlyThirteenPermanents() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, uprising.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Stensia Uprising");
        assertThat(findPermanents(player1, "Human")).hasSize(1);
    }

    @Test
    @DisplayName("Declining the sacrifice leaves Stensia Uprising on the battlefield")
    void mayDeclineSacrifice() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
    }

    @Test
    @DisplayName("Does not offer the sacrifice when the token makes fourteen permanents")
    void doesNotSacrificeWithMoreThanThirteenPermanents() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        resolveEndStepTrigger(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addUprisingWithElevenOtherPermanents() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());
        for (int i = 0; i < 11; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        return uprising;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveEndStepTrigger(Player player) {
        advanceToEndStep(player);
        harness.passBothPriorities();
    }
}
