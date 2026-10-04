package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruesomeFate.class, AdantoVanguard.class, Forest.class})
class GruesomeFateTest extends BaseCardTest {

    @Test
    void eachOpponentLosesOneLifeForEachCreatureControllerControls() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AdantoVanguard());
        harness.castFromHand(player1, new GruesomeFate(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void doesNothingWhenControllerControlsNoCreatures() {
        harness.castFromHand(player1, new GruesomeFate(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void countsCreaturesThatEnterBeforeResolution() {
        harness.castFromHand(player1, new GruesomeFate(), "{2}{B}");
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new AdantoVanguard());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotCountCreaturesThatLeaveBeforeResolution() {
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.castFromHand(player1, new GruesomeFate(), "{2}{B}");
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
