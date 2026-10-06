package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfTheSwarm.class, AngelOfMercy.class})
class ScionOfTheSwarmTest extends BaseCardTest {

    @Test
    void putsCounterOnItWhenControllerGainsLife() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfTheSwarm());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOpponentGainsLife() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfTheSwarm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void separateLifeGainsEachPutOneCounter() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfTheSwarm());
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(scion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void eachScionGetsItsOwnCounterOnlyWhenItsTriggerResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScionOfTheSwarm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScionOfTheSwarm());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.stack).hasSize(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainingZeroLifeDoesNotTrigger() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfTheSwarm());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(scion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
