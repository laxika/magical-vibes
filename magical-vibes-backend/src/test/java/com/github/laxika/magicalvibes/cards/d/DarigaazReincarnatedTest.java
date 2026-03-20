package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileWithEggCountersInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DarigaazReincarnatedTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Darigaaz Reincarnated has exile-with-egg-counters replacement effect")
    void hasExileWithEggCountersReplacementEffect() {
        DarigaazReincarnated card = new DarigaazReincarnated();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof ExileWithEggCountersInsteadOfDyingEffect egg && egg.count() == 3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Darigaaz Reincarnated resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new DarigaazReincarnated()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darigaaz Reincarnated");
    }

    // ===== Death replacement: exile with egg counters =====

    @Test
    @DisplayName("When Darigaaz would die, it is exiled with 3 egg counters instead")
    void exiledWithEggCountersWhenWouldDie() {
        harness.addToBattlefield(player1, new DarigaazReincarnated());

        // Force sacrifice via Cruel Edict
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Not on battlefield
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");
        // NOT in graveyard — replacement effect
        harness.assertNotInGraveyard(player1, "Darigaaz Reincarnated");
        // In exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darigaaz Reincarnated"));
        // Has 3 egg counters tracked
        Card exiledCard = gd.playerExiledCards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Darigaaz Reincarnated"))
                .findFirst().orElseThrow();
        assertThat(gd.exiledCardEggCounters.get(exiledCard.getId())).isEqualTo(3);
        // Log confirms replacement
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Darigaaz Reincarnated") && log.contains("exiled with 3 egg counters"));
    }

    // ===== Upkeep counter removal =====

    @Test
    @DisplayName("At beginning of upkeep, one egg counter is removed")
    void upkeepRemovesOneEggCounter() {
        // Put Darigaaz in exile with 3 egg counters directly
        DarigaazReincarnated card = new DarigaazReincarnated();
        exileWithEggCounters(player1.getId(), card, 3);

        // Trigger upkeep for player1
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        // Counter decremented to 2
        assertThat(gd.exiledCardEggCounters.get(card.getId())).isEqualTo(2);
        // Still in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darigaaz Reincarnated"));
        // NOT on battlefield yet
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");
    }

    @Test
    @DisplayName("Darigaaz returns to the battlefield when last egg counter is removed")
    void returnsToBattlefieldWhenLastCounterRemoved() {
        DarigaazReincarnated card = new DarigaazReincarnated();
        exileWithEggCounters(player1.getId(), card, 1);

        // Trigger upkeep for player1
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        // Back on battlefield
        harness.assertOnBattlefield(player1, "Darigaaz Reincarnated");
        // Not in exile anymore
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Darigaaz Reincarnated"));
        // Counter tracking removed
        assertThat(gd.exiledCardEggCounters).doesNotContainKey(card.getId());
        // Log confirms return
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Darigaaz Reincarnated") && log.contains("returns to the battlefield"));
    }

    // ===== Full cycle: die → exile → 3 upkeeps → return =====

    @Test
    @DisplayName("Full lifecycle: Darigaaz dies, exiled with 3 counters, returns after 3 upkeeps")
    void fullLifecycle() {
        harness.addToBattlefield(player1, new DarigaazReincarnated());

        // Kill Darigaaz
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Verify exiled with 3 counters
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");
        Card exiledCard = gd.playerExiledCards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Darigaaz Reincarnated"))
                .findFirst().orElseThrow();
        assertThat(gd.exiledCardEggCounters.get(exiledCard.getId())).isEqualTo(3);

        // Upkeep 1: 3 → 2
        triggerUpkeep(player1);
        assertThat(gd.exiledCardEggCounters.get(exiledCard.getId())).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");

        // Upkeep 2: 2 → 1
        triggerUpkeep(player1);
        assertThat(gd.exiledCardEggCounters.get(exiledCard.getId())).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");

        // Upkeep 3: 1 → 0, return to battlefield
        triggerUpkeep(player1);
        harness.assertOnBattlefield(player1, "Darigaaz Reincarnated");
        assertThat(gd.exiledCardEggCounters).doesNotContainKey(exiledCard.getId());
    }

    // ===== Edge case: only triggers during controller's upkeep =====

    @Test
    @DisplayName("Egg counter is NOT removed during opponent's upkeep")
    void noCounterRemovalDuringOpponentUpkeep() {
        DarigaazReincarnated card = new DarigaazReincarnated();
        exileWithEggCounters(player1.getId(), card, 3);

        // Trigger upkeep for player2 (opponent)
        triggerUpkeep(player2);

        // Counter unchanged
        assertThat(gd.exiledCardEggCounters.get(card.getId())).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");
    }

    // ===== Edge case: Darigaaz can die and be exiled again after returning =====

    @Test
    @DisplayName("After returning, Darigaaz can die and be exiled with egg counters again")
    void canDieAndExileAgainAfterReturning() {
        DarigaazReincarnated card = new DarigaazReincarnated();
        exileWithEggCounters(player1.getId(), card, 1);

        // Return via upkeep
        triggerUpkeep(player1);
        harness.assertOnBattlefield(player1, "Darigaaz Reincarnated");

        // Kill it again
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Exiled again with 3 counters
        harness.assertNotOnBattlefield(player1, "Darigaaz Reincarnated");
        harness.assertNotInGraveyard(player1, "Darigaaz Reincarnated");
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darigaaz Reincarnated"));
        Card reExiledCard = gd.playerExiledCards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Darigaaz Reincarnated"))
                .findFirst().orElseThrow();
        assertThat(gd.exiledCardEggCounters.get(reExiledCard.getId())).isEqualTo(3);
    }

    // ===== Helpers =====

    private void exileWithEggCounters(UUID playerId, Card card, int counters) {
        gd.playerExiledCards
                .computeIfAbsent(playerId, k -> java.util.Collections.synchronizedList(new java.util.ArrayList<>()))
                .add(card);
        gd.exiledCardEggCounters.put(card.getId(), counters);
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability
    }
}
