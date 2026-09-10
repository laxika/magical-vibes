package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeroInTraining;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakandanRoyalGuard.class, GrizzlyBears.class, HeroInTraining.class})
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
        harness.setHand(player1, List.of(new WakandanRoyalGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent guard = findPermanent(player1, "Wakandan Royal Guard");
        harness.handlePermanentChosen(player1, guard.getId());
        harness.passBothPriorities();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castRoyalGuard(Permanent target) {
        harness.setHand(player1, List.of(new WakandanRoyalGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
