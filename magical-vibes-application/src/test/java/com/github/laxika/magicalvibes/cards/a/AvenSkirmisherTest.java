package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvenSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Aven Skirmisher has flying")
    void hasFlying() {
        Permanent skirmisher = addCreatureReady(player1, new AvenSkirmisher());

        assertThat(gqs.hasKeyword(gd, skirmisher, Keyword.FLYING)).isTrue();
    }
}
