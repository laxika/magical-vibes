package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HedronCrawler.class)
class HedronCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Hedron Crawler produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new HedronCrawler());
        crawler.setSummoningSick(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(crawler.isTapped()).isTrue();
    }
}
