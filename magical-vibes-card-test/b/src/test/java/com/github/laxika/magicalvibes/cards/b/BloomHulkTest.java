package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloomHulk.class})
class BloomHulkTest extends BaseCardTest {

    @Test
    @DisplayName("When Bloom Hulk enters, it proliferates")
    void proliferatesWhenItEnters() {
        Permanent hulk = addCreatureReady(player1, new BloomHulk());
        hulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new BloomHulk(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(hulk.getId()));

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canChooseNoPermanentsOrPlayers() {
        Permanent hulk = addCreatureReady(player1, new BloomHulk());
        hulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.castFromHand(player1, new BloomHulk(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void addsOneOfEveryExistingCounterKindToSelectedPermanentsOnly() {
        Permanent own = addCreatureReady(player1, new BloomHulk());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        own.setCounterCount(CounterType.STUN, 2);
        Permanent opposing = addCreatureReady(player2, new BloomHulk());
        opposing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent unselected = addCreatureReady(player2, new BloomHulk());
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new BloomHulk(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(own.getId(), opposing.getId()));

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(own.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Bloom Hulk").get(1).getCounters()).isEmpty();
    }

    @Test
    void canProliferatePlayersAndPermanentsTogether() {
        Permanent hulk = addCreatureReady(player1, new BloomHulk());
        hulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.castFromHand(player1, new BloomHulk(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(hulk.getId(), player1.getId()));

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void resolvesWithoutAChoiceWhenNothingHasCounters() {
        harness.castFromHand(player1, new BloomHulk(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Bloom Hulk").getCounters()).isEmpty();
    }
}
