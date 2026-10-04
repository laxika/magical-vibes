package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdleThoughts.class})
class IdleThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws nothing when the controller already has cards in hand")
    void noDrawWhenHandNotEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of(new IdleThoughts()));
        harness.setLibrary(player1, List.of(new IdleThoughts(), new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Condition not met on resolution → hand unchanged (still the single starting card).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Checks the empty-hand condition when the ability resolves")
    void noDrawWhenHandBecomesNonEmptyBeforeResolution() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new IdleThoughts()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can activate with a nonempty hand and draw if it becomes empty before resolution")
    void drawsWhenHandBecomesEmptyBeforeResolution() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of(new IdleThoughts()));
        IdleThoughts drawnCard = new IdleThoughts();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Stacked activations draw only once when the first draw fills the hand")
    void stackedActivationsRecheckHandIndividually() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IdleThoughts(), new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Uses the ability controller's hand during the opponent's turn")
    void drawsDuringOpponentsTurnWithOpponentHandNotEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        IdleThoughts opponentCard = new IdleThoughts();
        harness.setHand(player2, List.of(opponentCard));
        IdleThoughts drawnCard = new IdleThoughts();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
    }
}
