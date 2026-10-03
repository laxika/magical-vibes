package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlleyStrangler.class})
class AlleyStranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Menace prevents Alley Strangler from being blocked by one creature")
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new AlleyStrangler());
        addCreatureReady(player2, new AlleyStrangler());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows Alley Strangler to be blocked by two creatures")
    void menaceAllowsAtLeastTwoBlockers() {
        addCreatureReady(player1, new AlleyStrangler());
        Permanent firstBlocker = addCreatureReady(player2, new AlleyStrangler());
        Permanent secondBlocker = addCreatureReady(player2, new AlleyStrangler());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace does not require the defender to block")
    void menaceAllowsNoBlockersEvenWhenOneIsAvailable() {
        addCreatureReady(player1, new AlleyStrangler());
        addCreatureReady(player2, new AlleyStrangler());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Menace allows more than two blockers")
    void menaceAllowsThreeBlockers() {
        addCreatureReady(player1, new AlleyStrangler());
        Permanent firstBlocker = addCreatureReady(player2, new AlleyStrangler());
        Permanent secondBlocker = addCreatureReady(player2, new AlleyStrangler());
        Permanent thirdBlocker = addCreatureReady(player2, new AlleyStrangler());

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
