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
