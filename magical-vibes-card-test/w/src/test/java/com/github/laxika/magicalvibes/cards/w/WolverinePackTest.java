package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverinePack.class, GrizzlyBears.class, Unsummon.class})
class WolverinePackTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 2 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        pack.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 2 grants +2/+2 until end of turn")
    void twoBlockersGivesPlusTwo() {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        pack.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isEqualTo(2);
        assertThat(pack.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivesPlusFour() {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        pack.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isEqualTo(4);
        assertThat(pack.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        pack.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rampage bonus wears off at end of turn")
    void bonusExpiresAtEndOfTurn() {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        pack.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isEqualTo(2);
        assertThat(pack.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Removing a blocker before rampage resolves leaves no bonus with zero or one blocker")
    void removedBlockerIsNotCountedAtResolution(int blockerCount) {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        if (blockerCount == 2) {
            addCreatureReady(player2, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, blockerCount == 1
                ? List.of(new BlockerAssignment(0, 0))
                : List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
        harness.castInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(blockerCount - 1);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(pack.getPowerModifier()).isZero();
        assertThat(pack.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing a blocker after rampage resolves does not reduce the bonus")
    void resolvedBonusDoesNotChangeWhenBlockerLeaves() {
        Permanent pack = addCreatureReady(player1, new WolverinePack());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(pack.getPowerModifier()).isEqualTo(2);
        assertThat(pack.getToughnessModifier()).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(pack.getPowerModifier()).isEqualTo(2);
        assertThat(pack.getToughnessModifier()).isEqualTo(2);
    }
}
