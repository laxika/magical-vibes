package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorribleHordes.class, IronTuskElephant.class, Boomerang.class})
class HorribleHordesTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 1 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);
        addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isZero();
        assertThat(hordes.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 1 grants +1/+1 until end of turn")
    void twoBlockersGivesPlusOne() {
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);
        addCreatureReady(player2, new IronTuskElephant());
        addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isEqualTo(1);
        assertThat(hordes.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With three blockers Rampage 1 grants +2/+2 until end of turn")
    void threeBlockersGivesPlusTwo() {
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);
        addCreatureReady(player2, new IronTuskElephant());
        addCreatureReady(player2, new IronTuskElephant());
        addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isEqualTo(2);
        assertThat(hordes.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(hordes.getPowerModifier()).isZero();
        assertThat(hordes.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocker removed before the trigger resolves is not counted")
    void blockerRemovedBeforeTriggerResolvesIsNotCounted() {
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);
        addCreatureReady(player2, new IronTuskElephant());
        Permanent removedBlocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, removedBlocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isZero();
        assertThat(hordes.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing a permanent before the source does not change the blocker count")
    void permanentBeforeSourceLeavingBattlefieldDoesNotChangeBlockerCount() {
        Permanent leadingPermanent = addCreatureReady(player1, new IronTuskElephant());
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);
        addCreatureReady(player2, new IronTuskElephant());
        addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        ));

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, leadingPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isEqualTo(1);
        assertThat(hordes.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rampage bonus wears off at end-of-turn cleanup")
    void bonusWearsOffAtEndOfTurn() {
        Permanent hordes = addCreatureReady(player1, new HorribleHordes());
        hordes.setAttacking(true);
        addCreatureReady(player2, new IronTuskElephant());
        addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(hordes.getPowerModifier()).isEqualTo(1);
        assertThat(hordes.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(hordes.getPowerModifier()).isZero();
        assertThat(hordes.getToughnessModifier()).isZero();
    }
}
