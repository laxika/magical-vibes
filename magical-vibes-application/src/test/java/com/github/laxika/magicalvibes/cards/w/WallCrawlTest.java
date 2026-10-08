package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirrorEntity;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallCrawl.class, GiantSpider.class, GrizzlyBears.class, WallOfDust.class, MirrorEntity.class, Opalescence.class})
class WallCrawlTest extends BaseCardTest {

    @Test
    @DisplayName("Wall Crawl creates a 2/1 Spider with reach and gains life for each Spider")
    void entersWithSpiderAndGainsLifeForSpiders() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setHand(player1, List.of(new WallCrawl()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> spiders = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Spider".equals(permanent.getCard().getName()))
                .toList();

        assertThat(spiders).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, spiders.getFirst())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spiders.getFirst())).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spiders.getFirst(), Keyword.REACH)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Wall Crawl boosts only Spiders you control")
    void boostsOnlyControlledSpiders() {
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent nonSpider = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallCrawl());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSpider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spiders you control cannot be blocked by creatures with defender")
    void spidersCannotBeBlockedByDefenders() {
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        spider.setAttacking(true);
        Permanent defender = addCreatureReady(player2, new WallOfDust());
        harness.addToBattlefield(player1, new WallCrawl());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, defender), indexOf(player1, spider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Spiders you control can be blocked by creatures without defender")
    void spidersCanBeBlockedByNonDefenders() {
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        spider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallCrawl());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, spider))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    @Test
    void opponentSpidersDoNotIncreaseLifeGainOrReceiveBoost() {
        Permanent opposingSpider = addCreatureReady(player2, new GiantSpider());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WallCrawl()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gqs.getEffectivePower(gd, opposingSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSpider)).isEqualTo(4);
    }

    @Test
    void animatedWallCrawlThatBecomesSpiderBoostsItself() {
        Permanent crawl = animateWallCrawlAsSpider();

        assertThat(gqs.hasEffectiveSubtype(gd, crawl, CardSubtype.SPIDER)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crawl)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crawl)).isEqualTo(5);
    }

    @Test
    void removingWallCrawlRemovesBoostAndDefenderRestriction() {
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent crawl = harness.addToBattlefieldAndReturn(player1, new WallCrawl());
        Permanent defender = addCreatureReady(player2, new WallOfDust());
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(crawl);

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
        spider.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, defender), indexOf(player1, spider))));
        assertThat(defender.isBlocking()).isTrue();
    }

    @Test
    void animatedWallCrawlThatBecomesSpiderCannotBeBlockedByDefender() {
        Permanent crawl = animateWallCrawlAsSpider();
        Permanent defender = addCreatureReady(player2, new WallOfDust());
        crawl.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, defender), indexOf(player1, crawl)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    private Permanent animateWallCrawlAsSpider() {
        Permanent crawl = harness.addToBattlefieldAndReturn(player1, new WallCrawl());
        harness.addToBattlefield(player1, new Opalescence());
        Permanent entity = addCreatureReady(player1, new MirrorEntity());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, indexOf(player1, entity), 4, null);
        resolveAllTriggers();
        crawl.setSummoningSick(false);
        return crawl;
    }

}
