package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TundraWall.class)
class TundraWallTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Tundra Wall from attacking")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new TundraWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
