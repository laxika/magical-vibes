package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruffTriplets.class, LightningBolt.class})
class GruffTripletsTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken Gruff Triplets creates two token copies when it enters")
    void createsTwoTokenCopiesOnEntry() {
        harness.setHand(player1, List.of(new GruffTriplets()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> triplets = findPermanents(player1, "Gruff Triplets");
        assertThat(triplets).hasSize(3);
        assertThat(triplets.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("When a Gruff Triplets dies, each other controlled Gruff Triplets gets counters equal to its power")
    void deathPutsCountersOnControlledTriplets() {
        harness.addToBattlefield(player1, new GruffTriplets());
        harness.addToBattlefield(player1, new GruffTriplets());
        UUID dyingTripletsId = harness.getPermanentId(player1, "Gruff Triplets");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, dyingTripletsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> survivingTriplets = findPermanents(player1, "Gruff Triplets");
        assertThat(survivingTriplets).hasSize(1);
        assertThat(survivingTriplets.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
