package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PincerSpider.class)
class PincerSpiderTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnCounter() {
        harness.setHand(player1, List.of(new PincerSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pincer Spider")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new PincerSpider()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pincer Spider")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castWithKickerRequiresAdditionalThreeMana() {
        harness.setHand(player1, List.of(new PincerSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringBattlefieldWithoutBeingCastDoesNotPutOnCounter() {
        Permanent spider = harness.enterBattlefieldAndReturn(player1, new PincerSpider());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
