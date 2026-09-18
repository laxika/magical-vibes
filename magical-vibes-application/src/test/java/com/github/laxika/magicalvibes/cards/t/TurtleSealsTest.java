package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TurtleSeals.class)
class TurtleSealsTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent turtleSeals = addCreatureReady(player1, new TurtleSeals());

        declareAttackers(List.of(0));

        assertThat(turtleSeals.isTapped()).isFalse();
    }
}
