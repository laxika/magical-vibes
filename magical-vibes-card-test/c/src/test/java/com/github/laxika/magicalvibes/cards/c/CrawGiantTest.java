package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrawGiant.class, Squire.class})
class CrawGiantTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 2 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 2 grants +2/+2 until end of turn")
    void twoBlockersGivesPlusTwo() {
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        addCreatureReady(player2, new Squire());
        addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(2);
        assertThat(giant.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With three blockers Rampage 2 grants +4/+4 until end of turn")
    void threeBlockersGivesPlusFour() {
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        addCreatureReady(player2, new Squire());
        addCreatureReady(player2, new Squire());
        addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(4);
        assertThat(giant.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts the blockers still blocking when Rampage resolves")
    void countsBlockersAtResolution() {
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        Permanent removedBlocker = addCreatureReady(player2, new Squire());
        addCreatureReady(player2, new Squire());
        addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removedBlocker));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(2);
        assertThat(giant.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent giant = addCreatureReady(player1, new CrawGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The rampage bonus wears off at end of turn")
    void rampageBonusWearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        addCreatureReady(player2, new Squire());
        addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(2);

        giant.setAttacking(false);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rampage increases the damage available to trample over multiple blockers")
    void rampageBoostAppliesToTrampleDamage() {
        harness.setLife(player2, 20);
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        Permanent firstBlocker = addCreatureReady(player2, new Squire());
        Permanent secondBlocker = addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 1,
                player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 2,
                player2.getId(), 4));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
    }

    @Test
    @DisplayName("Removing all blockers after rampage resolves preserves its bonus and permits full trample damage")
    void removingBlockersAfterResolutionPreservesBonus() {
        harness.setLife(player2, 20);
        Permanent giant = addCreatureReady(player1, new CrawGiant());
        Permanent firstBlocker = addCreatureReady(player2, new Squire());
        Permanent secondBlocker = addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstBlocker);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondBlocker);
        });

        assertThat(giant.getPowerModifier()).isEqualTo(2);
        assertThat(giant.getToughnessModifier()).isEqualTo(2);
        resolveCombat();

        harness.assertLife(player2, 12);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
    }
}
