package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KavuRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste when no opponent controls a white or blue creature")
    void hasHasteWithoutMatchingOpponentCreature() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new HillGiant());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Loses haste while an opponent controls a white creature")
    void losesHasteToOpponentWhiteCreature() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste while an opponent controls a blue creature")
    void losesHasteToOpponentBlueCreature() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Regains haste when the opposing white creature leaves")
    void regainsHasteWhenMatchingOpponentCreatureLeaves() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent kavu = findPermanent(player1, "Kavu Runner");
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();

        Permanent angel = findPermanent(player2, "Serra Angel");
        gd.playerBattlefields.get(player2.getId()).remove(angel);

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
    }
}
