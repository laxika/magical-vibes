package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicShield;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.s.ShorelineRaider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuRunner.class, BenalishLancer.class, ShorelineRaider.class, KavuAggressor.class,
        AngelicShield.class})
class KavuRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste when no opponent controls a white or blue creature")
    void hasHasteWithoutMatchingOpponentCreature() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.addToBattlefield(player2, new KavuAggressor());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Loses haste while an opponent controls a white creature")
    void losesHasteToOpponentWhiteCreature() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new BenalishLancer());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste while an opponent controls a blue creature")
    void losesHasteToOpponentBlueCreature() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new ShorelineRaider());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Regains haste when the opposing white creature leaves")
    void regainsHasteWhenMatchingOpponentCreatureLeaves() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new BenalishLancer());

        Permanent kavu = findPermanent(player1, "Kavu Runner");
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();

        Permanent lancer = findPermanent(player2, "Benalish Lancer");
        gd.playerBattlefields.get(player2.getId()).remove(lancer);

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Keeps haste while an opponent controls a white or blue noncreature permanent")
    void keepsHasteAgainstOpponentNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KavuRunner());
        harness.addToBattlefield(player2, new AngelicShield());

        Permanent kavu = findPermanent(player1, "Kavu Runner");

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
    }
}
