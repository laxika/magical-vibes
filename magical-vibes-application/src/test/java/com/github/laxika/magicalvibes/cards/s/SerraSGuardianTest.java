package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerraSGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control gain vigilance")
    void grantsVigilanceToOtherOwnCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new SerraSGuardian());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to an opponent's creatures")
    void doesNotGrantVigilanceToOpponentsCreatures() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new SerraSGuardian());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The vigilance grant ends when Serra's Guardian leaves the battlefield")
    void removesVigilanceWhenGuardianLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent guardian = addCreatureReady(player1, new SerraSGuardian());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(guardian);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }
}
