package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtmosphericGreenhouse.class, GrizzlyBears.class})
class AtmosphericGreenhouseTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a +1/+1 counter on each creature its controller controls")
    void enteringPutsCountersOnControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Station uses the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Eight charge counters make the Spacecraft an artifact creature with flying and trample")
    void eightChargeCountersUnlockAbilities() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());

        greenhouse.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, greenhouse)).isFalse();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.TRAMPLE)).isFalse();

        greenhouse.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, greenhouse)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.TRAMPLE)).isTrue();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
