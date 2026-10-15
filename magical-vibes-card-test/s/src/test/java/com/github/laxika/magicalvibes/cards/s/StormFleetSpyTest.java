package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormFleetSpy.class})
class StormFleetSpyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers draw when raid is met (attacked this turn)")
    void etbTriggersWithRaid() {
        markAttackedThisTurn();
        castStormFleetSpy();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Fleet Spy");
    }

    @Test
    @DisplayName("ETB raid trigger draws a card for controller")
    void etbDrawsCardWithRaid() {
        markAttackedThisTurn();
        castStormFleetSpy();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        castStormFleetSpy();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        harness.assertOnBattlefield(player1, "Storm Fleet Spy");

        // Hand size unchanged (no draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast);
    }

    @Test
    @DisplayName("An opponent attacking does not satisfy the controller's raid condition")
    void opponentAttackingDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castStormFleetSpy();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast);
    }

    @Test
    @DisplayName("Raid draw resolves even after the Spy leaves the battlefield")
    void drawResolvesAfterSourceLeaves() {
        markAttackedThisTurn();
        StormFleetSpy drawnCard = new StormFleetSpy();
        harness.setLibrary(player1, List.of(drawnCard));
        castStormFleetSpy();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();

        var spy = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(spy.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castStormFleetSpy();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Storm Fleet Spy");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with raid")
    void stackEmptyAfterResolution() {
        markAttackedThisTurn();
        castStormFleetSpy();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetSpy() {
        harness.setHand(player1, List.of(new StormFleetSpy()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
    }
}
