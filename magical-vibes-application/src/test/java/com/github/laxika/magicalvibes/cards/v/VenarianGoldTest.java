package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.g.GreenManaBattery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenarianGold.class, DurkwoodBoars.class, GreenManaBattery.class, Boomerang.class})
class VenarianGoldTest extends BaseCardTest {

    @Test
    @DisplayName("Venarian Gold taps the enchanted creature and puts X sleep counters on it")
    void entersWithXSleepCountersAndTapsCreature() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());

        castVenarianGold(creature, 2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(2);
    }

    @Test
    @DisplayName("Venarian Gold with X=0 taps the creature but does not add sleep counters")
    void zeroXDoesNotKeepCreatureTapped() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());

        castVenarianGold(creature, 0);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isZero();

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isZero();
    }

    @Test
    @DisplayName("Sleep counters prevent untapping until the enchanted creature's upkeep removes them")
    void sleepCountersPreventUntappingAndAreRemovedAtUpkeep() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());
        castVenarianGold(creature, 2);

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isZero();

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature's controller's upkeep removes a sleep counter")
    void onlyEnchantedControllersUpkeepRemovesCounter() {
        Permanent creature = addCreatureReady(player2, new DurkwoodBoars());
        castVenarianGold(creature, 2);

        advanceToUpkeep(player1);
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(1);
    }

    @Test
    @DisplayName("Venarian Gold can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GreenManaBattery());
        harness.setHand(player1, List.of(new VenarianGold()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A pending upkeep ability removes a sleep counter after Venarian Gold leaves")
    void upkeepAbilityResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());
        castVenarianGold(creature, 2);
        Permanent aura = findPermanent(player1, "Venarian Gold");

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        assertThat(countPermanents(player1, "Venarian Gold")).isZero();

        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sleep counters alone do not prevent untapping after Venarian Gold leaves")
    void creatureUntapsAfterAuraLeavesDespiteRemainingCounters() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());
        castVenarianGold(creature, 2);
        Permanent aura = findPermanent(player1, "Venarian Gold");
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(2);
    }

    @Test
    @DisplayName("The enter ability still taps and adds counters after Venarian Gold leaves")
    void enterAbilityResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());
        harness.setHand(player1, List.of(new VenarianGold()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        gs.playCard(gd, player1, 0, 2, creature.getId(), null);
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Venarian Gold");
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isZero();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.SLEEP)).isEqualTo(2);
    }

    private void castVenarianGold(Permanent creature, int xValue) {
        harness.setHand(player1, List.of(new VenarianGold()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);

        gs.playCard(gd, player1, 0, xValue, creature.getId(), null);
        resolveAllTriggers();
    }
}
