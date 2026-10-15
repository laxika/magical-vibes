package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShipwreckLooter.class, Forest.class})
class ShipwreckLooterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB with raid met: accepting may draws then discards a card")
    void etbWithRaidAcceptMay() {
        harness.setLibrary(player1, List.of(new Forest()));
        markAttackedThisTurn();
        castShipwreckLooter();

        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve ETB trigger

        // MayEffect prompts controller
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        // Net: drew 1, discarded 1 → hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("ETB with raid met: declining may does nothing")
    void etbWithRaidDeclineMay() {
        harness.setLibrary(player1, List.of(new Forest()));
        markAttackedThisTurn();
        castShipwreckLooter();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No draw, no discard
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        castShipwreckLooter();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        harness.assertOnBattlefield(player1, "Shipwreck Looter");
    }

    @Test
    @DisplayName("An opponent attacking does not satisfy raid")
    void opponentsAttackDoesNotSatisfyRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castShipwreckLooter();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Shipwreck Looter");
    }

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castShipwreckLooter();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Shipwreck Looter");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with raid")
    void stackEmptyAfterResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        markAttackedThisTurn();
        castShipwreckLooter();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The drawn card can be discarded from an initially empty hand")
    void canDiscardDrawnCard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        markAttackedThisTurn();
        castShipwreckLooter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Discard may select a card that was in hand before the draw")
    void canKeepDrawnCardAndDiscardAnother() {
        ShipwreckLooter drawn = new ShipwreckLooter();
        Forest discarded = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new Forest()));
        markAttackedThisTurn();
        castShipwreckLooter();
        harness.setHand(player1, List.of(discarded));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castShipwreckLooter() {
        harness.castFromHand(player1, new ShipwreckLooter(), "{1}{U}");
    }
}
