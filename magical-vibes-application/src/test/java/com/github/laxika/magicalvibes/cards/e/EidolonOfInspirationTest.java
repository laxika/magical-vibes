package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EidolonOfInspiration.class, GrizzlyBears.class})
class EidolonOfInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives a target creature you control +2/+0")
    void beginningOfCombatBoostsTargetCreature() {
        addCreatureReady(player1, new EidolonOfInspiration());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Beginning of combat can target the source creature")
    void canTargetSourceCreature() {
        Permanent eidolon = addCreatureReady(player1, new EidolonOfInspiration());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(eidolon.getId());

        harness.handlePermanentChosen(player1, eidolon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(2);
    }

    @Test
    @DisplayName("The beginning-of-combat boost wears off at end of turn")
    void beginningOfCombatBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new EidolonOfInspiration());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new EidolonOfInspiration());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
