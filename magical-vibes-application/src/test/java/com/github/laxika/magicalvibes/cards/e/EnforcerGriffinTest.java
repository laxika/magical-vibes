package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EnforcerGriffin.class)
class EnforcerGriffinTest extends BaseCardTest {

    @Test
    void hasFlying() {
        Permanent griffin = addCreatureReady(player1, new EnforcerGriffin());

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FLYING)).isTrue();
    }
}
