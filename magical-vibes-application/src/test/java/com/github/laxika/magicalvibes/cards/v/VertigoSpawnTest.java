package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VertigoSpawn.class, GhorClanSavage.class})
class VertigoSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking taps the blocked creature and keeps it from untapping next turn")
    void blockingTapsAndLocksBlockedCreature() {
        Permanent blocker = addCreatureReady(player2, new VertigoSpawn());
        Permanent attacker = addCreatureReady(player1, new GhorClanSavage());

        declareBlockers(List.of(0), List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each creature blocked by a Vertigo Spawn gets its own trigger")
    void eachBlockedCreatureGetsItsOwnTrigger() {
        addCreatureReady(player2, new VertigoSpawn());
        addCreatureReady(player2, new VertigoSpawn());
        Permanent firstAttacker = addCreatureReady(player1, new GhorClanSavage());
        Permanent secondAttacker = addCreatureReady(player1, new GhorClanSavage());

        declareBlockers(List.of(0, 1), List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        assertThat(firstAttacker.isTapped()).isTrue();
        assertThat(firstAttacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(secondAttacker.isTapped()).isTrue();
        assertThat(secondAttacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The blocked creature untaps normally after its next untap step")
    void skipAppliesOnlyToTheNextUntapStep() {
        addCreatureReady(player2, new VertigoSpawn());
        Permanent attacker = addCreatureReady(player1, new GhorClanSavage());

        declareBlockers(List.of(0), List.of(new BlockerAssignment(0, 0)));
        attacker.tap();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No creature blocked means Vertigo Spawn does not trigger")
    void doesNotTriggerWithoutBlocking() {
        addCreatureReady(player2, new VertigoSpawn());
        addCreatureReady(player1, new GhorClanSavage());

        declareBlockers(List.of(0), List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocking taps an attacker that was untapped before the trigger resolved")
    void tapsUntappedAttackerOnResolution() {
        addCreatureReady(player2, new VertigoSpawn());
        Permanent attacker = addCreatureReady(player1, new GhorClanSavage());

        declareBlockers(List.of(0), List.of(new BlockerAssignment(0, 0)));
        attacker.untap();
        assertThat(attacker.isTapped()).isFalse();

        resolveAllTriggers();

        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two Spawns blocking one creature prevent only its next untap")
    void multipleBlockTriggersDoNotSkipAdditionalUntapSteps() {
        addCreatureReady(player2, new VertigoSpawn());
        addCreatureReady(player2, new VertigoSpawn());
        Permanent attacker = addCreatureReady(player1, new GhorClanSavage());

        declareBlockers(List.of(0), List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    private void declareBlockers(List<Integer> attackerIndices, List<BlockerAssignment> assignments) {
        declareAttackersAndPrepareBlockers(attackerIndices);
        gs.declareBlockers(gd, player2, assignments);
    }
}
