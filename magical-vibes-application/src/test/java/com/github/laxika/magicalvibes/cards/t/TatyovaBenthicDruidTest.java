package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TatyovaBenthicDruidTest extends BaseCardTest {

    // ===== Triggers when controller plays a land =====

    @Test
    @DisplayName("Gains 1 life and draws a card when controller plays a land")
    void triggersOnControllerLand() {
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        // Trigger on stack (one StackEntry with both effects)
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        // Hand: setHand(1) -> play land(0) -> draw 1 = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Does not trigger for opponent's lands =====

    @Test
    @DisplayName("Does not trigger when opponent plays a land")
    void doesNotTriggerForOpponentLands() {
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // No trigger — only cares about controller's lands
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    // ===== Two Tatyovas trigger separately =====

    @Test
    @DisplayName("Two Tatyovas each trigger separately when controller plays a land")
    void twoTatyovasEachTrigger() {
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        // Two triggers (one per Tatyova)
        assertThat(gd.stack).hasSize(2);

        // Resolve the first landfall trigger. The two legendary Tatyovas then break the legend rule
        // (CR 704.5j, a state-based action): the controller keeps one and the other is put into the
        // graveyard. Both landfall triggers still resolve — they triggered before one Tatyova left.
        harness.passBothPriorities();
        UUID keptTatyova = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tatyova, Benthic Druid"))
                .findFirst().orElseThrow().getId();
        harness.handlePermanentChosen(player1, keptTatyova);
        harness.passBothPriorities(); // Resolve the second trigger

        harness.assertLife(player1, 22);
        // Hand: setHand(1) -> play land(0) -> draw 1 + draw 1 = 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // The legend rule leaves exactly one Tatyova on the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tatyova, Benthic Druid")).count())
                .isEqualTo(1);
    }

    // ===== Triggers on each land separately =====

    @Test
    @DisplayName("Triggers each time controller plays a land on separate turns")
    void triggersOnEachLand() {
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // First land
        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        // Hand: setHand(1) -> play land(0) -> draw 1 = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        // Reset for new turn
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Second land (setHand replaces entire hand)
        harness.setHand(player1, List.of(new Island()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        // Hand: setHand(1) -> play land(0) -> draw 1 = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
