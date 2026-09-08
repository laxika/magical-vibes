package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JhovallQueenTest extends BaseCardTest {

    @Test
    void vigilanceKeepsJhovallQueenUntappedAfterAttacking() {
        Permanent queen = addCreatureReady(player1, new JhovallQueen());

        declareAttackers(player1, List.of(0));

        assertThat(queen.isTapped()).isFalse();
    }
}
