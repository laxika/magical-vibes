package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonEatingDog.class, MuYanling.class, ChandraNalaar.class})
class MoonEatingDogTest extends BaseCardTest {

    @Test
    void doesNotHaveFlyingWithoutYanlingPlaneswalker() {
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new MoonEatingDog());

        assertThat(gqs.hasKeyword(gd, dog, Keyword.FLYING)).isFalse();
    }

    @Test
    void hasFlyingWhenYouControlYanlingPlaneswalker() {
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new MoonEatingDog());
        harness.addToBattlefield(player1, new MuYanling());

        assertThat(gqs.hasKeyword(gd, dog, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotCountOpponentOrNonYanlingPlaneswalkers() {
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new MoonEatingDog());
        harness.addToBattlefield(player2, new MuYanling());
        harness.addToBattlefield(player1, new ChandraNalaar());

        assertThat(gqs.hasKeyword(gd, dog, Keyword.FLYING)).isFalse();
    }
}
