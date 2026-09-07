package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SovereignOkinecAhau.class, GrizzlyBears.class})
class SovereignOkinecAhauTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on each creature equal to its power above base power")
    void putsCountersEqualToPowerAboveBasePower() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent oneAboveBase = addCreatureReady(player1, new GrizzlyBears());
        oneAboveBase.setPowerModifier(1);
        Permanent threeAboveBase = addCreatureReady(player1, new GrizzlyBears());
        threeAboveBase.setPowerModifier(3);
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sovereign)));
        resolveAllTriggers();

        assertThat(oneAboveBase.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(threeAboveBase.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(unmodified.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sovereign.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Uses current power and base power at trigger resolution")
    void usesResolutionCharacteristics() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sovereign)));
        creature.setPowerModifier(2);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
