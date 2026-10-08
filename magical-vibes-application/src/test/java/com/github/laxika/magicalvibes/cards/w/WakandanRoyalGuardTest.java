package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.h.HeroInTraining;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakandanRoyalGuard.class, GrizzlyBears.class, HeroInTraining.class, HardenedScales.class})
class WakandanRoyalGuardTest extends BaseCardTest {

    @Test
    void putsOneCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRoyalGuard(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsTwoCountersOnAnotherHero() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());

        castRoyalGuard(hero);

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsOnlyOneCounterOnItselfBecauseItIsNotAnotherHero() {
        harness.castFromHand(player1, new WakandanRoyalGuard(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent guard = findPermanent(player1, "Wakandan Royal Guard");
        harness.handlePermanentChosen(player1, guard.getId());
        harness.passBothPriorities();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsTwoCountersOnOpponentsHero() {
        Permanent hero = harness.addToBattlefieldAndReturn(player2, new HeroInTraining());

        castRoyalGuard(hero);

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsThreeCountersOnAnotherHeroWithHardenedScales() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());

        castRoyalGuard(hero);

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void stillPutsTwoCountersOnAnotherHeroAfterSourceLeaves() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroInTraining());
        harness.castFromHand(player1, new WakandanRoyalGuard(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hero.getId());
        Permanent guard = findPermanent(player1, "Wakandan Royal Guard");
        gd.playerBattlefields.get(player1.getId()).remove(guard);
        gd.playerGraveyards.get(player1.getId()).add(guard.getCard());

        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castRoyalGuard(Permanent target) {
        harness.castFromHand(player1, new WakandanRoyalGuard(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
