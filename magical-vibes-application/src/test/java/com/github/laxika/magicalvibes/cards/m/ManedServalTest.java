package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ManedServal.class)
class ManedServalTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenAttacking() {
        Permanent serval = addCreatureReady(player1, new ManedServal());

        declareAttackers(List.of(0));

        assertThat(serval.isTapped()).isFalse();
    }
}
