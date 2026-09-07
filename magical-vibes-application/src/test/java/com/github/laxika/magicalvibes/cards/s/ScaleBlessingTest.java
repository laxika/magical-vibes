package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaleBlessing.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})
class ScaleBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Bolsters the least-tough creature and adds another counter to all controlled creatures with counters")
    void bolstersThenAddsCountersToAllCounterBearers() {
        Permanent leastToughCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent alreadyModifiedCreature = addCreatureReady(player1, new HillGiant());
        alreadyModifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ownNoncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        ownNoncreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castScaleBlessing();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(alreadyModifiedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownNoncreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when you control no creatures")
    void doesNothingWithoutCreatures() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castScaleBlessing();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castScaleBlessing() {
        harness.setHand(player1, List.of(new ScaleBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
