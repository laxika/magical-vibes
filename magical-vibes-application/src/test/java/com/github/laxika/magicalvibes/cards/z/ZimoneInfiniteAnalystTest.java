package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneInfiniteAnalyst.class, FanningTheFlames.class, GrizzlyBears.class})
class ZimoneInfiniteAnalystTest extends BaseCardTest {

    @Test
    @DisplayName("The first X spell costs {1} less for each +1/+1 counter on Zimone")
    void firstXSpellGetsCounterBasedCostReduction() {
        Permanent zimone = addZimoneWithCounters(2);
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A non-X spell does not use the first-X-spell reduction")
    void nonXSpellDoesNotConsumeReduction() {
        Permanent zimone = addZimoneWithCounters(1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new FanningTheFlames()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(zimone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Only the first X spell each turn gets the reduction")
    void onlyFirstXSpellEachTurnIsReduced() {
        addZimoneWithCounters(1);
        harness.setHand(player1, List.of(new FanningTheFlames(), new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(findZimone().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    private Permanent addZimoneWithCounters(int counters) {
        Permanent zimone = harness.addToBattlefieldAndReturn(player1, new ZimoneInfiniteAnalyst());
        zimone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return zimone;
    }

    private Permanent findZimone() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Zimone, Infinite Analyst"))
                .findFirst()
                .orElseThrow();
    }
}
