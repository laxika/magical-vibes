package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SejiriMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have first strike or lifelink without a Plains")
    void noKeywordsWithoutPlains() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Has first strike and lifelink while its controller controls a Plains")
    void gainsKeywordsWithPlains() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Plains does not grant the keywords")
    void opponentPlainsDoesNotGrantKeywords() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses the keywords when its controller no longer controls a Plains")
    void losesKeywordsWhenPlainsLeaves() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new SejiriMerfolk());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(plains);

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isFalse();
    }
}
