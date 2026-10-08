package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.t.TobiasAndrion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfHeat.class, TobiasAndrion.class})
class WallOfHeatTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Heat can't attack because it has defender")
    void cannotAttack() {
        addCreatureReady(player1, new WallOfHeat());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wall of Heat can block and survives combat with Tobias Andrion")
    void defenderAllowsBlocking() {
        addCreatureReady(player1, new TobiasAndrion());
        Permanent wall = addCreatureReady(player2, new WallOfHeat());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();

        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wall of Heat");
        harness.assertOnBattlefield(player1, "Tobias Andrion");
    }
}
