package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZarOjanenScionOfEfrava.class, GrizzlyBears.class, HillGiant.class,
        Forest.class, Island.class, Mountain.class, Plains.class})
class ZarOjanenScionOfEfravaTest extends BaseCardTest {

    @Test
    @DisplayName("When Zar Ojanen becomes tapped, it puts counters on creatures below your Domain")
    void becomingTappedPutsCountersOnCreaturesBelowDomain() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        tapAndResolve(zar);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(zar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Domain count is evaluated when the triggered ability resolves")
    void domainIsEvaluatedAtResolution() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player1, new Forest());

        zar.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, zar));
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
