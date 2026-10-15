package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KiloApogeeMind.class, GrizzlyBears.class})
class KiloApogeeMindTest extends BaseCardTest {

    @Test
    @DisplayName("When Kilo becomes tapped, it proliferates")
    void proliferatesWhenItBecomesTapped() {
        Permanent kilo = addCreatureReady(player1, new KiloApogeeMind());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kilo)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping another permanent does not trigger Kilo's ability")
    void anotherPermanentBecomingTappedDoesNotTrigger() {
        Permanent kilo = addCreatureReady(player1, new KiloApogeeMind());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(kilo.isTapped()).isFalse();
    }
}
