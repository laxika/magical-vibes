package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailureComplyTest extends BaseCardTest {

    @Test
    @DisplayName("Failure returns target spell to its owner's hand")
    void failureReturnsSpellToHand() {
        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new FailureComply()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, shockId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        harness.assertNotInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Failure");
    }

    @Test
    @DisplayName("Comply from graveyard prompts a name choice then locks opponents from casting it")
    void complyLocksNamedSpellForOpponents() {
        harness.setGraveyard(player1, List.of(new FailureComply()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.context()).isInstanceOf(ChoiceContext.OpponentsCantCastNamedSpellsUntilNextTurnChoice.class);

        harness.handleListChoice(player1, "Shock");

        assertThat(gd.opponentsCantCastNamedSpellsUntilControllerNextTurn.get(player1.getId()))
                .contains("Shock");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Failure") || c.getName().equals("Comply"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Failure"));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Comply does not prevent the controller from casting the named spell")
    void complyDoesNotLockController() {
        harness.setGraveyard(player1, List.of(new FailureComply()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shock");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Comply lock ends at the start of the controller's next turn")
    void complyLockEndsOnControllersNextTurn() {
        harness.setGraveyard(player1, List.of(new FailureComply()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shock");

        assertThat(gd.opponentsCantCastNamedSpellsUntilControllerNextTurn.get(player1.getId()))
                .contains("Shock");

        // Advance through player1's cleanup into player2's turn — lock still active.
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.opponentsCantCastNamedSpellsUntilControllerNextTurn.get(player1.getId()))
                .contains("Shock");

        // Advance through player2's turn into player1's next turn — lock clears.
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.opponentsCantCastNamedSpellsUntilControllerNextTurn.containsKey(player1.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Comply requires sorcery timing")
    void complyRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new FailureComply()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
