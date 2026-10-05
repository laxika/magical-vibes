package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuTitan.class, Repulse.class})
class KavuTitanTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrTrample() {
        harness.castFromHand(player1, new KavuTitan(), "{1}{G}");
        harness.passBothPriorities();

        Permanent kavuTitan = findKavuTitan();
        assertThat(kavuTitan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, kavuTitan, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithThreeCountersAndTrample() {
        harness.setHand(player1, List.of(new KavuTitan()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavuTitan = findKavuTitan();
        assertThat(kavuTitan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kavuTitan, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void bouncedKickedTitanRecastWithoutKickerLosesCountersAndTrample() {
        harness.setHand(player1, List.of(new KavuTitan()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent original = findKavuTitan();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new KavuTitan()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, original.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kavu Titan");
        harness.assertInHand(player1, "Kavu Titan");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recast = findKavuTitan();
        assertThat(recast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, recast, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent findKavuTitan() {
        return findPermanent(player1, "Kavu Titan");
    }
}
