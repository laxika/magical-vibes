package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResplendentGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without the city's blessing does not put a counter on it")
    void doesNotGetCounterWithoutCityBlessing() {
        Permanent griffin = addCreatureReady(player1, new ResplendentGriffin());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking with the city's blessing puts a +1/+1 counter on it")
    void getsCounterWithCityBlessing() {
        Permanent griffin = addCreatureReady(player1, new ResplendentGriffin());
        gd.playersWithCityBlessing.add(player1.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ascend grants the city's blessing when the tenth permanent enters")
    void ascendGrantsCityBlessing() {
        Permanent griffin = addCreatureReady(player1, new ResplendentGriffin());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
