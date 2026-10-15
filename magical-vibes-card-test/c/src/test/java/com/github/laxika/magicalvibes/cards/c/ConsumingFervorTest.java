package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
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

@CardUsed({ConsumingFervor.class, ThoseWhoServe.class, LuxaRiverShrine.class})
class ConsumingFervorTest extends BaseCardTest {


    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConsumingFervor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("Creature returns to base stats when Consuming Fervor is removed")
    void boostStopsWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConsumingFervor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }


    @Test
    @DisplayName("At controller's upkeep, enchanted creature gets a -1/-1 counter")
    void upkeepPutsMinusCounter() {
        Permanent creature = addCreatureReady(player1, new ThoseWhoServe());

        harness.setHand(player1, List.of(new ConsumingFervor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore + 1);
        // Net P/T: base 2/4, +3/+3 boost, one -1/-1 counter => 4/6.
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1, new ThoseWhoServe());

        harness.setHand(player1, List.of(new ConsumingFervor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(countersBefore);
    }


    @Test
    @DisplayName("Cannot target a noncreature permanent with Consuming Fervor")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new ThoseWhoServe());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        harness.setHand(player1, List.of(new ConsumingFervor()));
        harness.addMana(player1, ManaColor.RED, 1);


        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("An opponent's enchanted creature gets the counter during its controller's upkeep")
    void opponentCreatureTriggersOnItsControllersUpkeep() {
        Permanent creature = addCreatureReady(player2, new ThoseWhoServe());
        harness.setHand(player1, List.of(new ConsumingFervor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Enchanting an opponent's creature does not trigger during the Aura controller's upkeep")
    void opponentCreatureDoesNotTriggerOnAuraControllersUpkeep() {
        Permanent creature = addCreatureReady(player2, new ThoseWhoServe());
        harness.setHand(player1, List.of(new ConsumingFervor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
