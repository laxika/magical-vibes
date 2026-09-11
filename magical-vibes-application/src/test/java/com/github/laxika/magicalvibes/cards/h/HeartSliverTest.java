package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartSliver.class, MetallicSliver.class, MoggConscripts.class})
class HeartSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Heart Sliver grants itself haste (it is a Sliver)")
    void grantsSelfHaste() {
        Permanent sliver = addCreatureReady(player1, new HeartSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to another Sliver you control")
    void grantsHasteToOtherSliver() {
        addCreatureReady(player1, new HeartSliver());
        Permanent otherSliver = addCreatureReady(player1, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to an opponent's Sliver too")
    void grantsHasteToOpponentSliver() {
        addCreatureReady(player1, new HeartSliver());
        Permanent opponentSliver = addCreatureReady(player2, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new HeartSliver());
        Permanent nonSliver = addCreatureReady(player1, new MoggConscripts());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Slivers lose Heart Sliver's haste when it leaves the battlefield")
    void losesHasteWhenSourceLeaves() {
        Permanent heartSliver = addCreatureReady(player1, new HeartSliver());
        Permanent otherSliver = addCreatureReady(player1, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(heartSliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isFalse();
    }
}
