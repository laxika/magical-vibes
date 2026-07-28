package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DireWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("No banding without a Plains")
    void noBandingWithoutPlains() {
        harness.addToBattlefield(player1, new DireWolves());

        Permanent wolves = findPermanent(player1, "Dire Wolves");
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Has banding while controlling a Plains")
    void hasBandingWithPlains() {
        harness.addToBattlefield(player1, new DireWolves());
        harness.addToBattlefield(player1, new Plains());

        Permanent wolves = findPermanent(player1, "Dire Wolves");
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Loses banding when the Plains leaves")
    void losesBandingWhenPlainsLeaves() {
        harness.addToBattlefield(player1, new DireWolves());
        harness.addToBattlefield(player1, new Plains());

        Permanent wolves = findPermanent(player1, "Dire Wolves");
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Plains"));
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Plains doesn't grant banding")
    void opponentPlainsDoesNotCount() {
        harness.addToBattlefield(player1, new DireWolves());
        harness.addToBattlefield(player2, new Plains());

        Permanent wolves = findPermanent(player1, "Dire Wolves");
        assertThat(gqs.hasKeyword(gd, wolves, Keyword.BANDING)).isFalse();
    }
}
