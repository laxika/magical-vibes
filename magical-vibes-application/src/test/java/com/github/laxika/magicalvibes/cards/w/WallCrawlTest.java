package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfDust;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallCrawl.class, GiantSpider.class, GrizzlyBears.class, WallOfDust.class})
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
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        Permanent nonSpider = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallCrawl());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSpider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spiders you control cannot be blocked by creatures with defender")
    void spidersCannotBeBlockedByDefenders() {
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        spider.setAttacking(true);
        Permanent defender = addReadyCreature(player2, new WallOfDust());
        harness.addToBattlefield(player1, new WallCrawl());

        beginBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, defender), indexOf(player1, spider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Spiders you control can be blocked by creatures without defender")
    void spidersCanBeBlockedByNonDefenders() {
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        spider.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallCrawl());

        beginBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, spider))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
