package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BridledBighorn;
import com.github.laxika.magicalvibes.cards.c.CloudspireSkycycle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiriamHerdWhisperer.class, BridledBighorn.class, CloudspireSkycycle.class, GrizzlyBears.class})
class MiriamHerdWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, Miriam gives your Mounts and Vehicles hexproof")
    void givesMountsAndVehiclesHexproofDuringYourTurn() {
        addCreatureReady(player1, new MiriamHerdWhisperer());
        Permanent mount = addCreatureReady(player1, new BridledBighorn());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        Permanent nonMatchingPermanent = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent nonMatchingCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonMatchingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
