package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.setHand(player1, List.of(new CentaurCourser()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
