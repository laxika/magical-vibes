package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CrystallineCrawler.class)
class CrystallineCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Converge puts one +1/+1 counter on Crystalline Crawler for each color spent")
    void entersWithCountersForColorsSpent() {
        harness.setHand(player1, List.of(new CrystallineCrawler()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent crawler = findCrawler();
        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter adds one mana of the chosen color")
    void removesCounterForAnyColorMana() {
        Permanent crawler = addReadyCrawler(1);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(pool.get(ManaColor.RED)).isEqualTo(manaBefore + 1);
        assertThat(crawler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping Crystalline Crawler puts a +1/+1 counter on it")
    void tappingAddsCounter() {
        Permanent crawler = addReadyCrawler(0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crawler.isTapped()).isTrue();
        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability cannot be activated without a +1/+1 counter")
    void cannotRemoveMissingCounter() {
        addReadyCrawler(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCrawler(int counters) {
        Permanent crawler = new Permanent(new CrystallineCrawler());
        crawler.setSummoningSick(false);
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player1.getId()).add(crawler);
        return crawler;
    }

    private Permanent findCrawler() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CrystallineCrawler)
                .findFirst()
                .orElseThrow();
    }
}
