package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BonepickerSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain deathtouch or lifelink below three opponent poison counters")
    void noAbilitiesBelowCorruptedThreshold() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new BonepickerSkirge());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThat(gqs.hasKeyword(gd, skirge, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch and lifelink when an opponent has three poison counters")
    void gainsAbilitiesAtCorruptedThreshold() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new BonepickerSkirge());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, skirge, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not count poison counters on its controller")
    void checksOpponentsOnly() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new BonepickerSkirge());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, skirge, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses the granted abilities when the opponent falls below three poison counters")
    void losesAbilitiesWhenConditionStopsBeingMet() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new BonepickerSkirge());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, skirge, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.LIFELINK)).isTrue();

        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThat(gqs.hasKeyword(gd, skirge, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.LIFELINK)).isFalse();
    }
}
