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

    @Test
    void triggerStillResolvesAfterTheTrapLeavesTheBattlefield() {
        var trap = harness.addToBattlefieldAndReturn(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new TheToymakersTrap()));
        harness.setLife(player2, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, trap));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "5");
        harness.handleListChoice(player2, "2");

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "The Toymaker's Trap");
    }

    @Test
    void chosenNumberIsPubliclyRevealedAfterAWrongGuess() {
        harness.addToBattlefield(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new TheToymakersTrap()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "5");
        int logSizeBeforeGuess = gd.gameLog.size();
        harness.handleListChoice(player2, "2");

        assertThat(gd.gameLog.subList(logSizeBeforeGuess, gd.gameLog.size()))
                .anySatisfy(entry -> assertThat(entry.plainText()).contains("5"));
    }

    @Test
    void exhaustedNumbersDoNothingAndLeaveTheTrapOnTheBattlefield() {
        harness.addToBattlefield(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new TheToymakersTrap(), new TheToymakersTrap(),
                new TheToymakersTrap(), new TheToymakersTrap(), new TheToymakersTrap(),
                new TheToymakersTrap()));
        harness.setLife(player2, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        for (int chosen = 1; chosen <= 5; chosen++) {
            gd.turnNumber = chosen + 1;
            advanceToUpkeep(player1);
            resolveAllTriggers();
            harness.handleListChoice(player1, Integer.toString(chosen));
            harness.handleListChoice(player2, chosen == 1 ? "2" : "1");
        }

        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 5);
        gd.turnNumber++;
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 5);
        harness.assertOnBattlefield(player1, "The Toymaker's Trap");
    }

    @Test
    void doesNotTriggerDuringAnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new TheToymakersTrap());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "The Toymaker's Trap");
    }

    @Test
    void correctGuessDoesNotLoseLifeOrDrawACard() {
        harness.addToBattlefield(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new TheToymakersTrap()));
        harness.setLife(player2, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "5");
        harness.handleListChoice(player2, "5");

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInGraveyard(player1, "The Toymaker's Trap");
    }

    @Test
    void returningToTheBattlefieldResetsPreviouslyChosenNumbers() {
        var trap = harness.addToBattlefieldAndReturn(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new TheToymakersTrap()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player2, "2");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, trap));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, trap.getCard());
        gd.turnNumber++;
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("1", "2", "3", "4", "5");
    }
}
