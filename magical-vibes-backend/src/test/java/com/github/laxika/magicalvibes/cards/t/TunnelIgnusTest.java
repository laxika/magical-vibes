package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TunnelIgnusTest extends BaseCardTest {

    @Test
    @DisplayName("First land played by opponent does not trigger Tunnel Ignus")
    void firstLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0); // plays land via playCard

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Second land entering under opponent's control triggers Tunnel Ignus for 3 damage")
    void secondLandTriggers() {
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Simulate a land having already entered the battlefield this turn (e.g. via Skyshroud Ranger)
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player2.getId(), k -> new ArrayList<>())
                .add(new Forest());

        // Play a land from hand — this is the second land entering
        harness.setHand(player2, List.of(new Mountain()));
        harness.castCreature(player2, 0);

        // Tunnel Ignus trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve Tunnel Ignus trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Tunnel Ignus does not trigger for controller's own lands")
    void doesNotTriggerForControllerLands() {
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Simulate a land having already entered the battlefield this turn for controller
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), k -> new ArrayList<>())
                .add(new Forest());

        // Play a land from hand — this is the second land for controller
        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        // No trigger should fire — Tunnel Ignus only cares about opponents
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Tunnel Ignus trigger separately dealing 3 damage each")
    void twoTunnelIgnusStack() {
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Simulate a land having already entered this turn
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player2.getId(), k -> new ArrayList<>())
                .add(new Forest());

        // Play second land
        harness.setHand(player2, List.of(new Mountain()));
        harness.castCreature(player2, 0);

        // Two triggers should be on the stack (one per Tunnel Ignus)
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Third land also triggers Tunnel Ignus")
    void thirdLandAlsoTriggers() {
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Simulate two lands having already entered this turn
        List<Forest> priorLands = List.of(new Forest(), new Forest());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), new ArrayList<>(priorLands));

        // Play a third land
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Trigger should still fire for the third land
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Non-land permanents entering do not count toward the land condition")
    void nonLandDoesNotCountTowardCondition() {
        harness.addToBattlefield(player1, new TunnelIgnus());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Simulate a creature (non-land) having entered this turn
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player2.getId(), k -> new ArrayList<>())
                .add(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        // Play a land — this is only the first LAND entering
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // No trigger — only 1 land has entered, the creature doesn't count
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
