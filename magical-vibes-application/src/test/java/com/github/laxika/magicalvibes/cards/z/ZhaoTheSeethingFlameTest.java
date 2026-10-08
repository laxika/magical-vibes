package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DeerDog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhaoTheSeethingFlame.class, DeerDog.class})
class ZhaoTheSeethingFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Menace prevents Zhao, the Seething Flame from being blocked by one creature")
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new ZhaoTheSeethingFlame());
        addCreatureReady(player2, new DeerDog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows Zhao, the Seething Flame to be blocked by two creatures")
    void menaceAllowsAtLeastTwoBlockers() {
        addCreatureReady(player1, new ZhaoTheSeethingFlame());
        Permanent firstBlocker = addCreatureReady(player2, new DeerDog());
        Permanent secondBlocker = addCreatureReady(player2, new DeerDog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace allows the defender to leave Zhao unblocked")
    void menaceAllowsNoBlockers() {
        addCreatureReady(player1, new ZhaoTheSeethingFlame());
        addCreatureReady(player2, new DeerDog());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Menace allows more than two blockers")
    void menaceAllowsThreeBlockers() {
        addCreatureReady(player1, new ZhaoTheSeethingFlame());
        Permanent firstBlocker = addCreatureReady(player2, new DeerDog());
        Permanent secondBlocker = addCreatureReady(player2, new DeerDog());
        Permanent thirdBlocker = addCreatureReady(player2, new DeerDog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(thirdBlocker.isBlocking()).isTrue();
    }
}
