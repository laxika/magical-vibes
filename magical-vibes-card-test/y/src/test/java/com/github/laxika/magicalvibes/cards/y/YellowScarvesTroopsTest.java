package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YellowScarvesTroops.class, ForestBear.class})
class YellowScarvesTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Yellow Scarves Troops cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new YellowScarvesTroops());
        addCreatureReady(player1, new ForestBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Yellow Scarves Troops can attack and be blocked normally")
    void canAttackAndBeBlocked() {
        Permanent troops = addCreatureReady(player1, new YellowScarvesTroops());
        Permanent bear = addCreatureReady(player2, new ForestBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(troops.isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bear.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Yellow Scarves Troops does not stop other creatures from blocking")
    void otherCreaturesCanStillBlock() {
        Permanent troops = addCreatureReady(player2, new YellowScarvesTroops());
        Permanent bear = addCreatureReady(player2, new ForestBear());
        addCreatureReady(player1, new ForestBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(bear.isBlocking()).isTrue();
        assertThat(troops.isBlocking()).isFalse();
    }
}
