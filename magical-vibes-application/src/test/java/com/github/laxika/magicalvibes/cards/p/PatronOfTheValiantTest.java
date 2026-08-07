package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatronOfTheValiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB adds a +1/+1 counter only to your creatures that already have one")
    void etbBoostsOnlyCountersCreatures() {
        Permanent withCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounter = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        castPatron();

        assertThat(withCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB does not touch opponent creatures with +1/+1 counters")
    void etbIgnoresOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castPatron();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Patron itself enters without a counter, so it gets none")
    void patronGetsNoCounter() {
        castPatron();

        assertThat(findPatron().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castPatron() {
        harness.setHand(player1, List.of(new PatronOfTheValiant()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPatron() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Patron of the Valiant"))
                .findFirst().orElseThrow();
    }
}
