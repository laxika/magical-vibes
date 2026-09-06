package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfEarth.class})
class WallOfEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Wall of Earth from attacking")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new WallOfEarth());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
