package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheToymakersTrap.class})
class TheToymakersTrapTest extends BaseCardTest {

    @Test
    void wrongGuessMakesOpponentLoseGuessedLifeAndDraws() {
        harness.addToBattlefield(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new TheToymakersTrap()));
        harness.setLife(player2, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice numberChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(numberChoice.playerId()).isEqualTo(player1.getId());
        assertThat(numberChoice.options()).containsExactly("1", "2", "3", "4", "5");

        harness.handleListChoice(player1, "5");
        PendingInteraction.ColorChoice guessChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(guessChoice.playerId()).isEqualTo(player2.getId());
        assertThat(guessChoice.options()).containsExactly("1", "2", "3", "4", "5");

        harness.handleListChoice(player2, "2");

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertOnBattlefield(player1, "The Toymaker's Trap");
    }

    @Test
    void correctGuessSacrificesTheTrap() {
        harness.addToBattlefield(player1, new TheToymakersTrap());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "3");
        harness.handleListChoice(player2, "3");

        harness.assertNotOnBattlefield(player1, "The Toymaker's Trap");
        harness.assertInGraveyard(player1, "The Toymaker's Trap");
    }

    @Test
    void previouslyChosenNumberIsNotOfferedAgain() {
        harness.addToBattlefield(player1, new TheToymakersTrap());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player2, "2");

        gd.turnNumber++;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice numberChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(numberChoice.options()).doesNotContain("1");
        assertThat(numberChoice.options()).containsExactly("2", "3", "4", "5");
    }
}
