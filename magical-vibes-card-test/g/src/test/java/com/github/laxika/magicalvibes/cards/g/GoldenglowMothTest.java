package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenglowMoth.class, BriarberryCohort.class, HighGround.class})
class GoldenglowMothTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature and choosing yes gains 4 life")
    void blockingAndChoosingYesGainsLife() {
        addCreatureReady(player2, new GoldenglowMoth());
        addCreatureReady(player1, new GoldenglowMoth());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the trigger — should prompt for may ability
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Blocking a creature and choosing no does not gain life")
    void blockingAndChoosingNoDoesNotGainLife() {
        addCreatureReady(player2, new GoldenglowMoth());
        addCreatureReady(player1, new GoldenglowMoth());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger — decline the may ability
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Being blocked does not trigger Goldenglow Moth's ability")
    void beingBlockedDoesNotTrigger() {
        addCreatureReady(player1, new GoldenglowMoth());
        addCreatureReady(player2, new BriarberryCohort());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
    @Test
    @DisplayName("Blocking two attackers triggers the optional life gain only once")
    void blockingTwoAttackersTriggersOnlyOnce() {
        addCreatureReady(player1, new GoldenglowMoth());
        addCreatureReady(player1, new GoldenglowMoth());
        addCreatureReady(player2, new GoldenglowMoth());
        harness.addToBattlefield(player2, new HighGround());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
