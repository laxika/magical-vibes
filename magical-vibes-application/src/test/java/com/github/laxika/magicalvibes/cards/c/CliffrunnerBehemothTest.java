package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.cards.m.Meglonoth;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CliffrunnerBehemoth.class, GrizzlyBears.class, AvenSquire.class, Meglonoth.class})
class CliffrunnerBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste while controller controls a red permanent")
    void hasHasteWithRedPermanent() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.RED));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("No haste without a red permanent")
    void noHasteWithoutRedPermanent() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.GREEN));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's red permanent does not grant haste")
    void opponentRedPermanentDoesNotGrantHaste() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player2, coloredCreature(CardColor.RED));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste when the red permanent leaves the battlefield")
    void losesHasteWhenRedPermanentLeaves() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.RED));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getColors().contains(CardColor.RED));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Has lifelink while controller controls a white permanent")
    void hasLifelinkWithWhitePermanent() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.WHITE));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("No lifelink without a white permanent")
    void noLifelinkWithoutWhitePermanent() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.GREEN));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A red permanent grants haste but not lifelink")
    void redGrantsHasteOnly() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player1, coloredCreature(CardColor.RED));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentWhitePermanentDoesNotGrantLifelink() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.addToBattlefield(player2, new AvenSquire());

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void whitePermanentGrantsOnlyLifelinkAndRemovingItRemovesLifelink() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new AvenSquire());

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(squire);

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void oneMulticoloredPermanentGrantsBothKeywordsOnlyToBehemoth() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        Permanent meglonoth = harness.addToBattlefieldAndReturn(player1, new Meglonoth());

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, meglonoth, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, meglonoth, Keyword.LIFELINK)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(meglonoth);

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void whiteCardInHandDoesNotGrantLifelink() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.setHand(player1, List.of(new AvenSquire()));

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.LIFELINK)).isFalse();
    }

    private Card coloredCreature(CardColor color) {
        Card card = new GrizzlyBears();
        card.setColors(List.of(color));
        return card;
    }

}
