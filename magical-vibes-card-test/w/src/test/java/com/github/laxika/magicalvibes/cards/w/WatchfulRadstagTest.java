package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatchfulRadstag.class, CentaurCourser.class})
class WatchfulRadstagTest extends BaseCardTest {

    @Test
    void evolvesAndCreatesTokenCopy() {
        Permanent radstag = harness.addToBattlefieldAndReturn(player1, new WatchfulRadstag());

        castCentaurCourser();

        assertThat(radstag.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Watchful Radstag"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                });
    }

    @Test
    void tokenCopyRetainsEvolveAndSelfEvolveTrigger() {
        harness.addToBattlefield(player1, new WatchfulRadstag());
        castCentaurCourser();

        castCentaurCourser();

        assertThat(countPermanents(player1, "Watchful Radstag")).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Watchful Radstag"))
                .hasSize(2);
    }

    private void castCentaurCourser() {
        harness.castFromHand(player1, new CentaurCourser(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void equalPowerAndToughnessDoNotTriggerEvolve() {
        Permanent radstag = harness.addToBattlefieldAndReturn(player1, new WatchfulRadstag());

        harness.enterBattlefieldAndReturn(player1, new WatchfulRadstag());

        assertThat(gd.stack).isEmpty();
        assertThat(radstag.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Watchful Radstag")).isEqualTo(2);
    }

    @Test
    void opponentsCreatureDoesNotTriggerEvolve() {
        Permanent radstag = harness.addToBattlefieldAndReturn(player1, new WatchfulRadstag());

        harness.enterBattlefieldAndReturn(player2, new CentaurCourser());

        assertThat(gd.stack).isEmpty();
        assertThat(radstag.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Watchful Radstag")).isEqualTo(1);
    }

    @Test
    void multipleEvolveTriggersRecheckStatsBeforeAddingCountersOrCreatingCopies() {
        Permanent radstag = harness.addToBattlefieldAndReturn(player1, new WatchfulRadstag());
        harness.enterBattlefieldAndReturn(player1, new CentaurCourser());
        harness.enterBattlefieldAndReturn(player1, new CentaurCourser());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(radstag.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Watchful Radstag")).isEqualTo(2);
    }
}
