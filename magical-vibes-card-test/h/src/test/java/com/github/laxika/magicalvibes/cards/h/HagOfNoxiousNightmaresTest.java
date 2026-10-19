package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfNoxiousNightmares.class, GrizzlyBears.class})
class HagOfNoxiousNightmaresTest extends BaseCardTest {

    @Test
    void grantsMenaceToWarlocksOnly() {
        Permanent hag = addCreatureReady(player1, new HagOfNoxiousNightmares());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, hag, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }
}
