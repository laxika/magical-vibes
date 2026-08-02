package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeistOfTheMoorsTest extends BaseCardTest {

    @Test
    void hasFlying() {
        Permanent geist = addCreatureReady(player1, new GeistOfTheMoors());

        assertThat(gqs.hasKeyword(gd, geist, Keyword.FLYING)).isTrue();
    }
}
