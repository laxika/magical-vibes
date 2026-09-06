package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrakTheHuntCaller.class, GrizzlyBears.class, HillGiant.class})
class SurrakTheHuntCallerTest extends BaseCardTest {

    @Test
    @DisplayName("Formidable does not trigger below total power eight")
    void doesNotTriggerBelowTotalPowerEight() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Formidable lets Surrak grant haste to a creature you control")
    void grantsHasteAtTotalPowerEight() {
        Permanent surrak = addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(surrak.getId(), target.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Surrak's granted haste wears off at end of turn")
    void grantedHasteWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SurrakTheHuntCaller());
        Permanent target = addCreatureReady(player1, new HillGiant());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
