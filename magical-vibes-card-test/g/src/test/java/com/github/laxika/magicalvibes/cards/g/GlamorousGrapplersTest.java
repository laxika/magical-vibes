package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlamorousGrapplers.class, GrizzlyBears.class})
class GlamorousGrapplersTest extends BaseCardTest {

    @Test
    @DisplayName("Menace prevents Glamorous Grapplers from being blocked by one creature")
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new GlamorousGrapplers());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows Glamorous Grapplers to be blocked by two creatures")
    void menaceAllowsAtLeastTwoBlockers() {
        addCreatureReady(player1, new GlamorousGrapplers());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace allows Glamorous Grapplers to be blocked by three creatures")
    void menaceAllowsThreeBlockers() {
        addCreatureReady(player1, new GlamorousGrapplers());
        Permanent firstBlocker = addCreatureReady(player2, new GlamorousGrapplers());
        Permanent secondBlocker = addCreatureReady(player2, new GlamorousGrapplers());
        Permanent thirdBlocker = addCreatureReady(player2, new GlamorousGrapplers());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(thirdBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace does not require the defender to block")
    void menaceAllowsNoBlockers() {
        addCreatureReady(player1, new GlamorousGrapplers());
        Permanent availableBlocker = addCreatureReady(player2, new GlamorousGrapplers());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of());

        assertThat(availableBlocker.isBlocking()).isFalse();
        resolveCombat(player1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
