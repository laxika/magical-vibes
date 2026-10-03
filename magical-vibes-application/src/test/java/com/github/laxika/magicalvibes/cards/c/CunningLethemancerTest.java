package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CunningLethemancer.class, GrizzlyBears.class})
class CunningLethemancerTest extends BaseCardTest {

    // "At the beginning of your upkeep, each player discards a card."

    @Test
    @DisplayName("On controller's upkeep, each player chooses a card in APNAP order")
    void eachPlayerDiscards() {
        harness.addToBattlefield(player1, new CunningLethemancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        // Active player (player1) chooses first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        // Then the opponent chooses.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player with an empty hand discards nothing")
    void emptyHandDiscardsNothing() {
        harness.addToBattlefield(player1, new CunningLethemancer());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        // Player1 has no cards, so only player2 is prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Finishes resolving when the remaining player has an empty hand")
    void emptyRemainingPlayerHandFinishesResolution() {
        harness.addToBattlefield(player1, new CunningLethemancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new CunningLethemancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Chosen cards stay in hand until both players have chosen, then discard together")
    void discardsOnlyAfterBothPlayersChoose() {
        harness.addToBattlefield(player1, new CunningLethemancer());
        CunningLethemancer firstCard = new CunningLethemancer();
        CunningLethemancer secondCard = new CunningLethemancer();
        harness.setHand(player1, List.of(firstCard));
        harness.setHand(player2, List.of(secondCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        harness.assertNotInGraveyard(player1, "Cunning Lethemancer");

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cunning Lethemancer");
        harness.assertInGraveyard(player2, "Cunning Lethemancer");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both players having empty hands finishes the trigger without a choice")
    void bothHandsEmptyFinishesResolution() {
        harness.addToBattlefield(player1, new CunningLethemancer());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
