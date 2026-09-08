package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MundasVanguard.class, HadaFreeblade.class, GrizzlyBears.class})
class MundasVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and puts a +1/+1 counter on each creature you control")
    void cohortPutsCountersOnEachCreatureYouControl() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null);
        harness.passBothPriorities();

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent vanguard = addCreatureReady(player1, new MundasVanguard());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(vanguard), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
