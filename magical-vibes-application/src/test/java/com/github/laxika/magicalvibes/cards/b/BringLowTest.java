package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonscaleBoon;
import com.github.laxika.magicalvibes.cards.w.WoollyLoxodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BringLow.class, WoollyLoxodon.class, BribersPurse.class, DragonscaleBoon.class})
class BringLowTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToCreatureWithoutPlusOnePlusOneCounter() {
        Permanent target = addCreatureReady(player2, new WoollyLoxodon());

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageToCreatureWithPlusOnePlusOneCounter() {
        Permanent target = addCreatureReady(player2, new WoollyLoxodon());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BribersPurse());
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multiplePlusOnePlusOneCountersStillResultInFiveDamage() {
        Permanent target = addCreatureReady(player2, new WoollyLoxodon());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void otherStatCountersDoNotIncreaseDamage() {
        Permanent target = addCreatureReady(player2, new WoollyLoxodon());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void canTargetControllersOwnCreature() {
        Permanent target = addCreatureReady(player1, new WoollyLoxodon());

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void checksCountersAddedInResponseAtResolution() {
        Permanent target = addCreatureReady(player2, new WoollyLoxodon());
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new DragonscaleBoon()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void checksCountersRemovedBeforeResolution() {
        Permanent target = addCreatureReady(player2, new WoollyLoxodon());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void castBringLow(Permanent target) {
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
