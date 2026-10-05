package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerforatingArtist.class, LlanowarElves.class, Swamp.class})
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
        Permanent creature = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Raid lets the opponent discard a card instead")
    void raidLetsOpponentDiscardCard() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Llanowar Elves");
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

    @Test
    @DisplayName("Opponent may lose life even when sacrifice and discard are available")
    void opponentMayDeclineBothAlternatives() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lose 3 life");

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land cannot be sacrificed to avoid the raid penalty")
    void landIsNotASacrificeAlternative() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    @DisplayName("Raid does not trigger during the opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Artist triggers separately after an attack")
    void multipleArtistsEachCauseAPenalty() {
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.addToBattlefield(player1, new PerforatingArtist());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        advanceToEndStep();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
