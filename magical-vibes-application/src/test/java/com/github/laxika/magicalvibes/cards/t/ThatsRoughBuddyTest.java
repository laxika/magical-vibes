package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThatsRoughBuddy.class, CarrionFeeder.class, Forest.class, GrizzlyBears.class, ZuranOrb.class})
class ThatsRoughBuddyTest extends BaseCardTest {

    @Test
    void putsOneCounterAndDrawsWithoutCreatureLeaving() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void putsTwoCountersAfterCreatureLeavesUnderYourControl() {
        Permanent feeder = harness.addToBattlefieldAndReturn(player1, new CarrionFeeder());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void aNoncreatureLeavingDoesNotEnableTheBonus() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ThatsRoughBuddy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ThatsRoughBuddy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
