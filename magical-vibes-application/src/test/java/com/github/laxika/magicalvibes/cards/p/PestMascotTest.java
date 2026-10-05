package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.w.WitherbloomCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestMascot.class, AngelOfMercy.class, WitherbloomCharm.class})
class PestMascotTest extends BaseCardTest {

    @Test
    void putsOneCounterWhenControllerGainsLife() {
        harness.addToBattlefield(player1, new PestMascot());
        Permanent pestMascot = findPermanent(player1, "Pest Mascot");
        assertThat(pestMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(pestMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void separateLifeGainEventsEachAddOneCounterRegardlessOfAmount() {
        harness.addToBattlefield(player1, new PestMascot());
        Permanent pestMascot = findPermanent(player1, "Pest Mascot");
        harness.setHand(player1, List.of(new WitherbloomCharm(), new WitherbloomCharm()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        assertThat(pestMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(pestMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castInstant(player1, 0, 1, null);
        resolveAllTriggers();

        harness.assertLife(player1, 30);
        assertThat(pestMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentsLifeGainDoesNotTriggerMascot() {
        harness.addToBattlefield(player1, new PestMascot());
        harness.setHand(player2, List.of(new WitherbloomCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, 1, null);
        resolveAllTriggers();

        harness.assertLife(player2, 25);
        assertThat(findPermanent(player1, "Pest Mascot").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachControlledMascotGetsItsOwnCounter() {
        harness.addToBattlefield(player1, new PestMascot());
        harness.addToBattlefield(player1, new PestMascot());
        harness.addToBattlefield(player2, new PestMascot());
        harness.setHand(player1, List.of(new WitherbloomCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 1, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pest Mascot"))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(findPermanent(player2, "Pest Mascot").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
