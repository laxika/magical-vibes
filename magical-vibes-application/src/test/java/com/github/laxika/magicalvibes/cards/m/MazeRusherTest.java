package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MazeRusherTest extends BaseCardTest {

    @Test
    @DisplayName("A multicolored creature you control gains haste")
    void grantsHasteToOwnMulticoloredCreature() {
        harness.addToBattlefield(player1, new MazeRusher());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, multicolored

        Permanent ambusher = findPermanent(player1, "Qasali Ambusher");
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A monocolored creature you control does not gain haste")
    void doesNotGrantHasteToMonocoloredCreature() {
        harness.addToBattlefield(player1, new MazeRusher());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, monocolored

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored creature does not gain haste")
    void doesNotGrantHasteToOpponentCreature() {
        harness.addToBattlefield(player1, new MazeRusher());
        harness.addToBattlefield(player2, new WoollyThoctar()); // {R}{G}{W}, opponent

        Permanent thoctar = findPermanent(player2, "Woolly Thoctar");
        assertThat(gqs.hasKeyword(gd, thoctar, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The grant ends when Maze Rusher leaves the battlefield")
    void grantEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new MazeRusher());
        harness.addToBattlefield(player1, new QasaliAmbusher());

        Permanent ambusher = findPermanent(player1, "Qasali Ambusher");
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.HASTE)).isTrue();

        Permanent rusher = findPermanent(player1, "Maze Rusher");
        gd.playerBattlefields.get(player1.getId()).remove(rusher);

        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.HASTE)).isFalse();
    }
}
