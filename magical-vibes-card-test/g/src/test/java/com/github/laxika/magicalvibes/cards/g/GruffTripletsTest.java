package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
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

@CardUsed({GruffTriplets.class, LightningBolt.class, CandyGrapple.class})
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

        harness.castAndResolveInstant(player2, 0, dyingTripletsId);
        harness.passBothPriorities();

        List<Permanent> survivingTriplets = findPermanents(player1, "Gruff Triplets");
        assertThat(survivingTriplets).hasSize(1);
        assertThat(survivingTriplets.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A token copy dying strengthens both remaining Triplets but not an opponent's Triplets")
    void tokenDeathStrengthensOnlyControlledTriplets() {
        harness.setHand(player1, List.of(new GruffTriplets(), new LightningBolt()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new GruffTriplets());
        Permanent token = findPermanents(player1, "Gruff Triplets").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gruff Triplets")).hasSize(2)
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3));
        assertThat(findPermanents(player2, "Gruff Triplets").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger includes counters in the dying creature's last-known power")
    void deathUsesPowerIncludingCounters() {
        harness.addToBattlefield(player1, new GruffTriplets());
        harness.addToBattlefield(player1, new GruffTriplets());
        Permanent dying = findPermanents(player1, "Gruff Triplets").getFirst();
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gruff Triplets")).hasSize(1);
        assertThat(findPermanents(player1, "Gruff Triplets").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("A Triplets that dies with zero power puts no counters on survivors")
    void zeroPowerDeathAddsNoCounters() {
        harness.addToBattlefield(player1, new GruffTriplets());
        harness.addToBattlefield(player1, new GruffTriplets());
        UUID dyingId = harness.getPermanentId(player1, "Gruff Triplets");
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, dyingId);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gruff Triplets")).hasSize(1);
        assertThat(findPermanents(player1, "Gruff Triplets").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The entry trigger creates copies even if the original dies before it resolves")
    void entryTriggerSurvivesRemovalOfSource() {
        harness.setHand(player1, List.of(new GruffTriplets(), new CandyGrapple()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Gruff Triplets");
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gruff Triplets")).hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().isToken()).isTrue();
                    assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                });
    }
}
