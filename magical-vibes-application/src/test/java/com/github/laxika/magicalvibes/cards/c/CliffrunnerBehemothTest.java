package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CliffrunnerBehemothTest extends BaseCardTest {

    // ===== Haste from a red permanent =====

    @Test
    @DisplayName("Has haste while controller controls a red permanent")
    void hasHasteWithRedPermanent() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.RED));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("No haste without a red permanent")
    void noHasteWithoutRedPermanent() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.GREEN));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's red permanent does not grant haste")
    void opponentRedPermanentDoesNotGrantHaste() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player2, coloredCreature(CardColor.RED));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste when the red permanent leaves the battlefield")
    void losesHasteWhenRedPermanentLeaves() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.RED));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getColors().contains(CardColor.RED));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
    }

    // ===== Lifelink from a white permanent =====

    @Test
    @DisplayName("Has lifelink while controller controls a white permanent")
    void hasLifelinkWithWhitePermanent() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.WHITE));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("No lifelink without a white permanent")
    void noLifelinkWithoutWhitePermanent() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.GREEN));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    // ===== The two conditions are independent =====

    @Test
    @DisplayName("A red permanent grants haste but not lifelink")
    void redGrantsHasteOnly() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.RED));

        Permanent behemoth = findPermanent(player1, "Cliffrunner Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    // ===== Helper =====

    private Card coloredCreature(CardColor color) {
        Card card = new GrizzlyBears();
        card.setColors(List.of(color));
        return card;
    }

}
