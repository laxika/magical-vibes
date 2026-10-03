package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
    @DisplayName("Converge gives one +1/+1 counter for each distinct color spent")
    void entersWithCountersForDistinctColorsSpent() {
        harness.setHand(player1, List.of(new CrystallineCrawler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent crawler = findPermanent(player1, "Crystalline Crawler");
        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping puts a +1/+1 counter on Crystalline Crawler")
    void tappingAddsCounter() {
        Permanent crawler = addReadyCrawler(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(crawler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing a +1/+1 counter adds one mana of the chosen color")
    void removesCounterForAnyColorMana() {
        Permanent crawler = addReadyCrawler(player1, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter-to-mana ability cannot be activated without a counter")
    void cannotRemoveAbsentCounter() {
        addReadyCrawler(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyCrawler(Player player, int counters) {
        Permanent crawler = addCreatureReady(player, new CrystallineCrawler());
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return crawler;
    }
}
