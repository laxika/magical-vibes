package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(chaser.isRenowned()).isFalse();
        assertThat(gqs.hasKeyword(gd, chaser, Keyword.MENACE)).isFalse();
    }

    private void attackUnblocked() {
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }
}
