package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MazeSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Own multicolored creature gains vigilance")
    void ownMulticoloredCreatureGainsVigilance() {
        harness.addToBattlefield(player1, new MazeSentinel());
        harness.addToBattlefield(player1, new QasaliAmbusher());

        Permanent ambusher = findPermanent(player1, "Qasali Ambusher");
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to own monocolored creature")
    void doesNotGrantToMonocoloredCreature() {
        harness.addToBattlefield(player1, new MazeSentinel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant vigilance to opponent's multicolored creature")
    void doesNotGrantToOpponentMulticoloredCreature() {
        harness.addToBattlefield(player1, new MazeSentinel());
        harness.addToBattlefield(player2, new QasaliAmbusher());

        Permanent ambusher = findPermanent(player2, "Qasali Ambusher");
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is lost when Maze Sentinel leaves the battlefield")
    void keywordLostWhenSentinelRemoved() {
        harness.addToBattlefield(player1, new MazeSentinel());
        harness.addToBattlefield(player1, new QasaliAmbusher());

        Permanent ambusher = findPermanent(player1, "Qasali Ambusher");
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Maze Sentinel"));

        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isFalse();
    }
}
