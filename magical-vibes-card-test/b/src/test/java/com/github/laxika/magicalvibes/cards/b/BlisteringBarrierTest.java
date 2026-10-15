package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BlisteringBarrier.class)
class BlisteringBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Blistering Barrier cannot attack because it has defender")
    void cannotAttackBecauseOfDefender() {
        addCreatureReady(player1, new BlisteringBarrier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
