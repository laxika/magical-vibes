package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinGloryChaser.class, WallOfWood.class, YokedOx.class})
class GoblinGloryChaserTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to a player makes it renowned with a +1/+1 counter")
    void becomesRenowned() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());

        attackUnblocked();

        assertThat(chaser.isRenowned()).isTrue();
        assertThat(chaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not have menace before becoming renowned")
    void noMenaceBeforeRenown() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());

        assertThat(gqs.hasKeyword(gd, chaser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace once it is renowned")
    void menaceWhileRenowned() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());

        attackUnblocked();

        assertThat(gqs.hasKeyword(gd, chaser, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A blocked Goblin Glory Chaser never becomes renowned")
    void blockedDoesNotBecomeRenowned() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());
        addCreatureReady(player2, new WallOfWood());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(chaser.isRenowned()).isFalse();
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.MENACE)).isFalse();
    }

    @Test
    void subsequentCombatDamageDoesNotAddAnotherRenownCounter() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());
        attackUnblocked();

        harness.performUntapStep(player1);
        attackUnblocked();

        assertThat(chaser.isRenowned()).isTrue();
        assertThat(chaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 17);
    }

    @Test
    void removingTheCounterDoesNotRemoveRenownOrMenace() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());
        attackUnblocked();

        chaser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(chaser.isRenowned()).isTrue();
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.MENACE)).isTrue();
        harness.performUntapStep(player1);
        attackUnblocked();
        assertThat(chaser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void renownedChaserCannotBeBlockedByOnlyOneCreature() {
        addCreatureReady(player1, new GoblinGloryChaser());
        attackUnblocked();
        harness.performUntapStep(player1);
        addCreatureReady(player2, new YokedOx());
        addCreatureReady(player2, new YokedOx());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void renownedChaserCanBeBlockedByTwoCreatures() {
        Permanent chaser = addCreatureReady(player1, new GoblinGloryChaser());
        attackUnblocked();
        harness.performUntapStep(player1);
        Permanent first = addCreatureReady(player2, new YokedOx());
        Permanent second = addCreatureReady(player2, new YokedOx());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        harness.assertLife(player2, 19);
        assertThat(chaser.isRenowned()).isTrue();
        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void attackUnblocked() {
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }
}
