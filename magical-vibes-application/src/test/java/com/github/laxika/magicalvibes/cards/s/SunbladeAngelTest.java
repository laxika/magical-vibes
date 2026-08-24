package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SunbladeAngel.class)
class SunbladeAngelTest extends BaseCardTest {

    @Test
    void hasFlyingFirstStrikeVigilanceAndLifelink() {
        Permanent angel = addCreatureReady(player1, new SunbladeAngel());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.LIFELINK)).isTrue();
    }
}
