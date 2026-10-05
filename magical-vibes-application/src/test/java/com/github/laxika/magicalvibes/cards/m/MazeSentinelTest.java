package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazeSentinel.class, QasaliAmbusher.class, GrizzlyBears.class})
class MazeSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Own multicolored creature gains vigilance")
    void ownMulticoloredCreatureGainsVigilance() {
        harness.addToBattlefield(player1, new MazeSentinel());
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to own monocolored creature")
    void doesNotGrantToMonocoloredCreature() {
        harness.addToBattlefield(player1, new MazeSentinel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant vigilance to opponent's multicolored creature")
    void doesNotGrantToOpponentMulticoloredCreature() {
        harness.addToBattlefield(player1, new MazeSentinel());
        Permanent ambusher = harness.addToBattlefieldAndReturn(player2, new QasaliAmbusher());
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is lost when Maze Sentinel leaves the battlefield")
    void keywordLostWhenSentinelRemoved() {
        harness.addToBattlefield(player1, new MazeSentinel());
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Maze Sentinel"));

        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Sentinel and own multicolored attacker stay untapped, while monocolored attacker taps")
    void vigilancePreventsTappingDuringAttack() {
        Permanent sentinel = addCreatureReady(player1, new MazeSentinel());
        Permanent ambusher = addCreatureReady(player1, new QasaliAmbusher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));

        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(ambusher.isAttacking()).isTrue();
        assertThat(bears.isAttacking()).isTrue();
        assertThat(sentinel.isTapped()).isFalse();
        assertThat(ambusher.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multicolored creature already on the battlefield gains vigilance when Sentinel enters")
    void existingCreatureGainsVigilanceWhenSentinelEnters() {
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());
        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new MazeSentinel());

        assertThat(gqs.hasKeyword(gd, ambusher, Keyword.VIGILANCE)).isTrue();
    }
}
