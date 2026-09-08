package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FurnaceStrider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IchorplateGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Adds an oil counter to a creature entering with an oil counter")
    void addsOilCounterToOilBearingEnteringCreature() {
        harness.addToBattlefield(player1, new IchorplateGolem());
        harness.setHand(player1, List.of(new FurnaceStrider()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent strider = findPermanent(player1, "Furnace Strider");
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not add an oil counter to a creature entering without one")
    void doesNotAddOilCounterToCreatureWithoutOilCounter() {
        harness.addToBattlefield(player1, new IchorplateGolem());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Does not add an oil counter if the entering creature loses its oil counter before resolution")
    void doesNotAddOilCounterWhenOilCounterIsRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new IchorplateGolem());
        Permanent entering = new Permanent(new FurnaceStrider());
        entering.setCounterCount(CounterType.OIL, 2);
        gd.playerBattlefields.get(player1.getId()).add(entering);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkAllyCreatureEntersTriggers(
                    gd, player1.getId(), entering.getCard(), 0);
            entering.setCounterCount(CounterType.OIL, 0);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        assertThat(entering.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Gives own creatures with oil counters +1/+1")
    void boostsOwnCreaturesWithOilCounters() {
        harness.addToBattlefield(player1, new IchorplateGolem());
        Permanent oilBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        oilBears.setCounterCount(CounterType.OIL, 1);
        Permanent plainBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentOilBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentOilBears.setCounterCount(CounterType.OIL, 1);

        assertThat(gqs.getEffectivePower(gd, oilBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oilBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, plainBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentOilBears)).isEqualTo(2);

        oilBears.setCounterCount(CounterType.OIL, 0);
        assertThat(gqs.getEffectivePower(gd, oilBears)).isEqualTo(2);
    }
}
