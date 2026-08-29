package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerforatingArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Raid makes the opponent lose 3 life when they cannot sacrifice or discard")
    void raidMakesOpponentLoseLifeWhenNoAlternativeIsAvailable() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Raid lets the opponent sacrifice a nonland permanent instead")
    void raidLetsOpponentSacrificeNonlandPermanent() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Raid lets the opponent discard a card instead")
    void raidLetsOpponentDiscardCard() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Raid does not trigger when you did not attack")
    void raidDoesNotTriggerWithoutAttack() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
