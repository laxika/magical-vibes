package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorosChallenger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RampagingMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new RampagingMonument()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent monument = findMonument();
        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(monument.getEffectivePower()).isEqualTo(3);
        assertThat(monument.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when its controller casts a multicolored spell")
    void multicoloredSpellAddsCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.setHand(player1, List.of(new BorosChallenger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for a monocolored spell")
    void monocoloredSpellDoesNotAddCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's multicolored spell")
    void opponentMulticoloredSpellDoesNotAddCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BorosChallenger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addMonumentWithCounters() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new RampagingMonument());
        monument.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        return monument;
    }

    private Permanent findMonument() {
        return findPermanent(player1, "Rampaging Monument");
    }
}
