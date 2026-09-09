package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfHeatTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Heat can't attack because it has defender")
    void cannotAttack() {
        addCreatureReady(player1, new WallOfHeat());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
