package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({VillagePillagers.class, FountainOfYouth.class, GrizzlyBears.class})
class VillagePillagersTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield trigger deals wither damage only to opponents' creatures")
    void etbDamagesOpponentsCreaturesWithWither() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        castVillagePillagers();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("A creature with any counter dying creates one tapped Treasure")
    void counteredOpponentCreatureCreatesTappedTreasure() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dyingCreature.setMarkedDamage(3);
        harness.addToBattlefield(player1, new VillagePillagers());

        harness.runStateBasedActions();
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature without a counter does not create a Treasure")
    void creatureWithoutCounterDoesNotCreateTreasure() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setMarkedDamage(2);
        harness.addToBattlefield(player1, new VillagePillagers());

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Its own countered creature dying does not create a Treasure")
    void ownCounteredCreatureDoesNotCreateTreasure() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dyingCreature.setMarkedDamage(3);
        harness.addToBattlefield(player1, new VillagePillagers());

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Multiple counters on one dying creature still create only one Treasure")
    void multipleCountersCreateOneTreasure() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        dyingCreature.setMarkedDamage(5);
        harness.addToBattlefield(player1, new VillagePillagers());

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Each creature dying to the enter trigger creates a tapped Treasure")
    void etbCreatesTreasureForEachCreatureItKills() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        castVillagePillagers();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The death trigger still resolves after Village Pillagers leaves")
    void deathTriggerResolvesAfterSourceDies() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        dyingCreature.setMarkedDamage(1);
        Permanent pillagers = addCreatureReady(player1, new VillagePillagers());

        harness.runStateBasedActions();
        pillagers.setMarkedDamage(5);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Village Pillagers");
        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Village Pillagers sees a countered opposing creature die simultaneously with it")
    void simultaneousSourceAndOpponentDeathCreatesTreasure() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        dyingCreature.setMarkedDamage(1);
        Permanent pillagers = addCreatureReady(player1, new VillagePillagers());
        pillagers.setMarkedDamage(5);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Village Pillagers");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }
    private void castVillagePillagers() {
        harness.setHand(player1, List.of(new VillagePillagers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

}
