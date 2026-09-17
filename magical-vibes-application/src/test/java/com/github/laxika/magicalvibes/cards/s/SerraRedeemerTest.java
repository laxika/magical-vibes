package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraRedeemer.class, GrizzlyBears.class, HillGiant.class})
class SerraRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on another creature you control with power 2 or less")
    void putsCountersOnQualifyingCreature() {
        harness.addToBattlefield(player1, new SerraRedeemer());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForLargeCreature() {
        harness.addToBattlefield(player1, new SerraRedeemer());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpposingCreature() {
        harness.addToBattlefield(player1, new SerraRedeemer());
        harness.setHand(player1, List.of());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Does not trigger for Serra Redeemer itself entering")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new SerraRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
