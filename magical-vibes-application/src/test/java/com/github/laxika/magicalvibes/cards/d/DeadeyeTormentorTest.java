package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadeyeTormentor.class, JungleDelver.class})
class DeadeyeTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers discard when raid is met (attacked this turn)")
    void etbTriggersWithRaid() {
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Deadeye Tormentor");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB raid trigger makes target opponent discard a card")
    void etbMakesOpponentDiscardWithRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new JungleDelver())));
        markAttackedThisTurn();
        castDeadeyeTormentor();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Jungle Delver");
    }

    @Test
    @DisplayName("ETB raid trigger does nothing when opponent has empty hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        markAttackedThisTurn();
        castDeadeyeTormentor();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new JungleDelver())));
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack and no target prompt when raid is not met.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Creature is still on the battlefield
        harness.assertOnBattlefield(player1, "Deadeye Tormentor");

        // Opponent hand unchanged — still has the card
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Jungle Delver");
    }

    @Test
    @DisplayName("Raid remains satisfied after the attacker leaves the battlefield")
    void raidRemainsSatisfiedAfterAttackerLeaves() {
        harness.setHand(player2, List.of(new JungleDelver()));
        JungleDelver attacker = new JungleDelver();
        harness.addToBattlefield(player1, attacker);
        markAttackedThisTurn();
        // Set up the postcombat state after the attacking creature has died.
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(attacker);
        castDeadeyeTormentor();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Jungle Delver");
    }

    @Test
    @DisplayName("An opponent attacking does not satisfy the controller's raid")
    void opponentsAttackDoesNotEnableRaid() {
        harness.setHand(player2, List.of(new JungleDelver()));
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castDeadeyeTormentor();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Jungle Delver");
    }

    @Test
    @DisplayName("Opponent chooses exactly one card from a larger hand")
    void opponentChoosesOneCard() {
        harness.setHand(player2, List.of(new JungleDelver(), new DeadeyeTormentor()));
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Jungle Delver");
        harness.assertInGraveyard(player2, "Deadeye Tormentor");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Deadeye Tormentor");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with raid")
    void stackEmptyAfterResolution() {
        harness.setHand(player2, new ArrayList<>(List.of(new JungleDelver())));
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger
        harness.handleCardChosen(player2, 0); // opponent chooses a card to discard

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger target prompt only offers opponents — choosing yourself is rejected")
    void cannotTargetYourself() {
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castDeadeyeTormentor() {
        harness.setHand(player1, List.of(new DeadeyeTormentor()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
    }
}
