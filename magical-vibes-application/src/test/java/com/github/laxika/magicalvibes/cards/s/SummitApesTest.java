package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SummitApesTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have menace without a Mountain")
    void noMountainNoMenace() {
        harness.addToBattlefield(player1, new SummitApes());

        Permanent apes = findPermanent(player1, "Summit Apes");

        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace while its controller controls a Mountain")
    void hasMenaceWithMountain() {
        harness.addToBattlefield(player1, new SummitApes());
        harness.addToBattlefield(player1, new Mountain());

        Permanent apes = findPermanent(player1, "Summit Apes");

        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Mountain does not grant menace")
    void opponentMountainDoesNotCount() {
        harness.addToBattlefield(player1, new SummitApes());
        harness.addToBattlefield(player2, new Mountain());

        Permanent apes = findPermanent(player1, "Summit Apes");

        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isFalse();
    }
}
