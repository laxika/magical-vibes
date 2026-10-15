package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.b.BarkweaveCrusher;
import com.github.laxika.magicalvibes.cards.v.ValiantVeteran;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraRedeemer.class, GrizzlyBears.class, HillGiant.class,
        BarkweaveCrusher.class, ValiantVeteran.class})
class SerraRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on another creature you control with power 2 or less")
    void putsCountersOnQualifyingCreature() {
        harness.addToBattlefield(player1, new SerraRedeemer());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForLargeCreature() {
        harness.addToBattlefield(player1, new SerraRedeemer());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
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

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Does not trigger for Serra Redeemer itself entering")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new SerraRedeemer(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(ValiantVeteran.class)
    @DisplayName("Does not trigger when a static bonus makes the entering creature's power greater than 2")
    void usesPowerIncludingStaticBonuses() {
        harness.addToBattlefield(player1, new SerraRedeemer());
        harness.addToBattlefield(player1, new ValiantVeteran());

        harness.castFromHand(player1, new ValiantVeteran(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof ValiantVeteran)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isZero());
    }

    @Test
    @CardUsed(BarkweaveCrusher.class)
    @DisplayName("Both Redeemers put counters on a creature even after the first trigger raises its power")
    void multipleRedeemersDoNotRecheckPowerAtResolution() {
        harness.addToBattlefield(player1, new SerraRedeemer());
        harness.addToBattlefield(player1, new SerraRedeemer());

        harness.castFromHand(player1, new BarkweaveCrusher(), "{3}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Barkweave Crusher")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An existing Redeemer triggers for another Redeemer entering")
    void triggersForAnotherRedeemer() {
        var existing = harness.addToBattlefieldAndReturn(player1, new SerraRedeemer());

        harness.castFromHand(player1, new SerraRedeemer(), "{3}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(existing.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }
}
