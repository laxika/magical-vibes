package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NishobaBrawler.class, Forest.class, Island.class, Plains.class, PrismaticOmen.class, Swamp.class})
class NishobaBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Nishoba Brawler's power equals your domain and its toughness remains 3")
    void powerUsesDomainAndToughnessRemainsThree() {
        Permanent brawler = addBrawlerReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nishoba Brawler counts distinct land types controlled by its controller")
    void countsDistinctControllerTypesOnly() {
        Permanent brawler = addBrawlerReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nishoba Brawler updates as the controller's lands change")
    void updatesWhenLandsChange() {
        Permanent brawler = addBrawlerReady(player1);

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(1);

        harness.addToBattlefield(player1, new PrismaticOmen());
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
    }

    private Permanent addBrawlerReady(Player player) {
        NishobaBrawler card = new NishobaBrawler();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
