package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreenwoodSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Greenwood Sentinel untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());

        declareAttackers(List.of(0));

        assertThat(sentinel.isTapped()).isFalse();
    }
}
