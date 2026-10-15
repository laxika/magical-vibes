package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BridledBighorn;
import com.github.laxika.magicalvibes.cards.l.LuxuriousLocomotive;
import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiriamHerdWhisperer.class, BridledBighorn.class, LuxuriousLocomotive.class, SterlingHound.class})
class MiriamHerdWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, Miriam gives your Mounts and Vehicles hexproof")
    void givesMountsAndVehiclesHexproofDuringYourTurn() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent mount = addCreatureReady(player1, new BridledBighorn());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new LuxuriousLocomotive());
        Permanent nonMatchingPermanent = addCreatureReady(player1, new SterlingHound());
        Permanent opponentMount = addCreatureReady(player2, new BridledBighorn());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, mount, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonMatchingPermanent, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentMount, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Mounts and Vehicles lose Miriam's hexproof during other turns")
    void onlyGivesHexproofDuringYourTurn() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent mount = addCreatureReady(player1, new BridledBighorn());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, mount, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Whenever a Mount or Vehicle attacks, Miriam puts a +1/+1 counter on it")
    void putsCounterOnAttackingMount() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent mount = addCreatureReady(player1, new BridledBighorn());
        Permanent nonMatchingCreature = addCreatureReady(player1, new SterlingHound());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonMatchingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterOnAttackingVehicle() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent vehicle = addCreatureReady(player1, new LuxuriousLocomotive());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void givesEachAttackingMountItsOwnCounter() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent first = addCreatureReady(player1, new BridledBighorn());
        Permanent second = addCreatureReady(player1, new BridledBighorn());
        Permanent idle = addCreatureReady(player1, new BridledBighorn());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(idle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPutCounterOnOpponentsAttackingMount() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent mount = addCreatureReady(player2, new BridledBighorn());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void vehicleLosesHexproofDuringOpponentsTurn() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new LuxuriousLocomotive());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HEXPROOF)).isTrue();
        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void attackTriggerResolvesAfterMiriamLeavesBattlefield() {
        Permanent miriam = addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent mount = addCreatureReady(player1, new BridledBighorn());

        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, miriam));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mount, Keyword.HEXPROOF)).isFalse();
    }
}
