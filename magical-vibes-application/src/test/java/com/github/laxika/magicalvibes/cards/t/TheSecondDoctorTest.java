package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSecondDoctor.class, GrizzlyBears.class})
class TheSecondDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Players have no maximum hand size")
    void playersHaveNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new TheSecondDoctor());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("An opponent who draws cannot attack the Doctor during their next turn")
    void opponentWhoDrawsCannotAttackDuringNextTurn() {
        harness.addToBattlefield(player1, new TheSecondDoctor());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleXValueChosen(player1, 1);
            harness.handleXValueChosen(player2, 1);
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(als.canAttackDefender(
                gd,
                gd.playerBattlefields.get(player2.getId()).get(0),
                player1.getId())).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(als.canAttackDefender(
                gd,
                gd.playerBattlefields.get(player2.getId()).get(0),
                player1.getId())).isFalse();
    }
}
