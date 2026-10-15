package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TangledIslet;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NishobaBrawler.class, Forest.class, Island.class, Plains.class, PrismaticOmen.class, Swamp.class, TangledIslet.class})
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

    @Test
    @DisplayName("A nonbasic land contributes both of its basic land types without double counting")
    void nonbasicLandContributesDistinctTypes() {
        Permanent brawler = addBrawlerReady(player1);
        Permanent islet = harness.addToBattlefieldAndReturn(player1, new TangledIslet());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(islet);

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prismatic Omen does not provide domain without a land")
    void landTypeGrantNeedsALand() {
        Permanent brawler = addBrawlerReady(player1);
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new PrismaticOmen());

        assertThat(gqs.getEffectivePower(gd, brawler)).isZero();

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(omen);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nishoba Brawler uses its new controller's lands after changing control")
    void powerFollowsCurrentController() {
        Permanent brawler = addBrawlerReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new TangledIslet());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(brawler);
        gd.playerBattlefields.get(player2.getId()).add(brawler);

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nishoba Brawler's power is defined in hand and graveyard")
    void domainAppliesOutsideBattlefield() {
        NishobaBrawler card = new NishobaBrawler();
        harness.setHand(player1, List.of(card));
        harness.addToBattlefield(player1, new TangledIslet());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card));
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);
    }

    private Permanent addBrawlerReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NishobaBrawler());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
