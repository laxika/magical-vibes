package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClericOfChillDepths.class, GrizzlyBears.class})
class ClericOfChillDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking makes the blocked creature skip its next untap")
    void blockingSkipsNextUntap() {
        addReadyBlocker(player2);
        Permanent attacker = addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The blocked creature remains tapped through only its next untap step")
    void blockingAffectsOnlyNextUntapStep() {
        addReadyBlocker(player2);
        Permanent attacker = addReadyAttacker(player1);
        attacker.tap();

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No block means no trigger")
    void doesNotTriggerWithoutBlock() {
        addReadyBlocker(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Clerics blocking the same attacker only prevent its next untap")
    void overlappingTriggersDoNotSkipAdditionalUntapSteps() {
        addReadyBlocker(player2);
        addReadyBlocker(player2);
        Permanent attacker = addCreatureReady(player1, new ClericOfChillDepths());
        attacker.setAttacking(true);
        attacker.tap();

        declareBlockers(List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The block trigger resolves even after the Cleric dies")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent blocker = addReadyBlocker(player2);
        Permanent attacker = addCreatureReady(player1, new ClericOfChillDepths());
        attacker.setAttacking(true);
        attacker.tap();

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        blocker.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped blocked creature is not tapped and consumes the restriction next untap")
    void untappedAttackerConsumesRestrictionWithoutBeingTapped() {
        addReadyBlocker(player2);
        Permanent attacker = addCreatureReady(player1, new ClericOfChillDepths());
        attacker.setAttacking(true);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(attacker.isTapped()).isFalse();

        harness.performUntapStep(player1);
        attacker.tap();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addReadyBlocker(Player player) {
        return addCreatureReady(player, new ClericOfChillDepths());
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
    }
}
