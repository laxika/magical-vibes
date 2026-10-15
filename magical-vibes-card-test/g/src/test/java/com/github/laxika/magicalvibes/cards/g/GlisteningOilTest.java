package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GlisteningOil.class, GrizzlyBears.class, Demystify.class})
class GlisteningOilTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gains infect")
    void enchantedCreatureGainsInfect() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Creature loses infect when Glistening Oil is removed")
    void creatureLosesInfectWhenOilRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent oilPerm = findPermanent(player1, "Glistening Oil");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, oilPerm.getId());
        harness.passBothPriorities(); // resolve death trigger

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
    }


    @Test
    @DisplayName("At controller's upkeep, enchanted creature gets a -1/-1 counter")
    void upkeepPutsMinusCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore + 1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore);
    }

    @Test
    @DisplayName("Counters accumulate over multiple upkeeps")
    void countersAccumulateOverUpkeeps() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Glistening Oil returns to owner's hand when destroyed")
    void returnsToHandWhenDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent oilPerm = findPermanent(player1, "Glistening Oil");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, oilPerm.getId());
        // Resolve death trigger — returns Glistening Oil from graveyard to hand
        harness.passBothPriorities();

        // Glistening Oil should be in owner's hand, not in graveyard
        harness.assertInHand(player1, "Glistening Oil");
        harness.assertNotInGraveyard(player1, "Glistening Oil");
    }

    @Test
    @DisplayName("Oil returns after its counters kill the enchanted creature")
    void returnsWhenEnchantedCreatureDiesFromCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Glistening Oil");
        harness.assertInGraveyard(player1, "Glistening Oil");
        resolveAllTriggers();
        harness.assertInHand(player1, "Glistening Oil");
        harness.assertNotInGraveyard(player1, "Glistening Oil");
    }

    @Test
    @DisplayName("Oil's upkeep follows its controller when enchanting an opponent's creature")
    void countersOpponentCreatureOnAuraControllersUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Oil in response does not stop its upkeep counter")
    void upkeepCounterResolvesAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent oil = findPermanent(player1, "Glistening Oil");

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, oil.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Glistening Oil");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Oil returns to its owner after dying under another player's control")
    void returnsToOwnerWhenControlledByOpponent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent oil = findPermanent(player1, "Glistening Oil");

        gd.playerBattlefields.get(player1.getId()).remove(oil);
        gd.playerBattlefields.get(player2.getId()).add(oil);
        gd.stolenCreatures.put(oil.getId(), player1.getId());
        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, oil.getId());
        harness.assertInGraveyard(player1, "Glistening Oil");
        resolveAllTriggers();

        harness.assertInHand(player1, "Glistening Oil");
        harness.assertNotInGraveyard(player1, "Glistening Oil");
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(card -> card.getName().equals("Glistening Oil"));
    }
}
