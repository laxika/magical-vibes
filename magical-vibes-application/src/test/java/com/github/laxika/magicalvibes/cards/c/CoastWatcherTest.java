package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CoastWatcher.class)
class CoastWatcherTest extends BaseCardTest {

    @Test
    void hasProtectionFromGreen() {
        Permanent coastWatcher = addCreatureReady(player1, new CoastWatcher());

        assertThat(gqs.hasProtectionFrom(gd, coastWatcher, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, coastWatcher, CardColor.RED)).isFalse();
    }
}
