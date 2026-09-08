package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralShadow.class, FemerefScouts.class})
class FeralShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Feral Shadow")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new FeralShadow());
        addCreatureReady(player2, new FemerefScouts());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
