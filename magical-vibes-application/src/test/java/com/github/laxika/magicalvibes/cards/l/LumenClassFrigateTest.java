package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumenClassFrigate.class, GrizzlyBears.class})
class LumenClassFrigateTest extends BaseCardTest {

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on the Frigate")
    void stationUsesTappedCreaturePowerAtResolution() {
        Permanent frigate = harness.addToBattlefieldAndReturn(player1, new LumenClassFrigate());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(frigate), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(frigate.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Charge thresholds apply the anthem and animate the Frigate")
    void chargeThresholds() {
        Permanent frigate = harness.addToBattlefieldAndReturn(player1, new LumenClassFrigate());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        frigate.setCounterCount(CounterType.CHARGE, 2);
        assertThat(gqs.isCreature(gd, frigate)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        frigate.setCounterCount(CounterType.CHARGE, 12);

        assertThat(gqs.isCreature(gd, frigate)).isTrue();
        assertThat(gqs.getEffectivePower(gd, frigate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, frigate)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, frigate, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, frigate, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationNeedsAnotherUntappedCreature() {
        Permanent frigate = harness.addToBattlefieldAndReturn(player1, new LumenClassFrigate());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(frigate), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
