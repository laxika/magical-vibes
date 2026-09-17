package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TemptWithGlory.class, GrizzlyBears.class})
class TemptWithGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the controller's creatures and rewards an accepting opponent")
    void acceptingOpponentGetsCountersAndRewardsController() {
        Permanent ownFirst = addCreatureReady(player1);
        Permanent ownSecond = addCreatureReady(player1);
        Permanent opponentFirst = addCreatureReady(player2);
        Permanent opponentSecond = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ownFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant the extra counters")
    void decliningOpponentDoesNotGetExtraCounters() {
        Permanent own = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);

        castTemptWithGlory();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void castTemptWithGlory() {
        harness.setHand(player1, List.of(new TemptWithGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
