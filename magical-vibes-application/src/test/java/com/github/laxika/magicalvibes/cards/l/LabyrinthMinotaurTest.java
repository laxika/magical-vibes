package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LabyrinthMinotaur.class, DwarvenTrader.class})
class LabyrinthMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a non-targeting triggered ability at the blocked creature")
    void blockTriggerPushesOntoStack() {
        Permanent minotaur = addReadyBlocker(player2);
        Permanent attacker = addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(minotaur.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving the block trigger makes the blocked creature skip its next untap")
    void resolvingSetsSkipUntapCount() {
        addReadyBlocker(player2);
        Permanent attacker = addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two block triggers affect only the next untap step")
    void multipleBlockTriggersAffectOnlyNextUntapStep() {
        addReadyBlocker(player2);
        addReadyBlocker(player2);
        Permanent attacker = addReadyBlocker(player1);
        attacker.setAttacking(true);
        attacker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No block means no block trigger")
    void doesNotTriggerWithoutBlock() {
        addReadyBlocker(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger does nothing if the blocked creature leaves before resolution")
    void triggerDoesNothingIfAttackerRemoved() {
        addReadyBlocker(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBlocker(Player player) {
        return addCreatureReady(player, new LabyrinthMinotaur());
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new DwarvenTrader());
        perm.setAttacking(true);
        return perm;
    }
}
