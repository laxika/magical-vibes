package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialWall.class})
class GlacialWallTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Glacial Wall from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new GlacialWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
