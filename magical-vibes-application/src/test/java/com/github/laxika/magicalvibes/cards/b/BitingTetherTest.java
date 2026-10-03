package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConsignToDream;
import com.github.laxika.magicalvibes.cards.d.DuskUrchins;
import com.github.laxika.magicalvibes.cards.i.IlluminatedFolio;
import com.github.laxika.magicalvibes.cards.i.IslebackSpawn;
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

@CardUsed({BitingTether.class, DuskUrchins.class, IlluminatedFolio.class, IslebackSpawn.class, ConsignToDream.class})
class BitingTetherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Biting Tether steals the enchanted creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new DuskUrchins());

        harness.setHand(player1, List.of(new BitingTether()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("At controller's upkeep, enchanted creature gets a -1/-1 counter")
    void upkeepPutsMinusCounter() {
        Permanent creature = addCreatureReady(player1, new DuskUrchins());

        harness.setHand(player1, List.of(new BitingTether()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int before = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1, new DuskUrchins());

        harness.setHand(player1, List.of(new BitingTether()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int before = creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(before);
    }

    @Test
    @DisplayName("Counters accumulate over multiple upkeeps")
    void countersAccumulateOverUpkeeps() {
        Permanent creature = addCreatureReady(player1, new DuskUrchins());

        harness.setHand(player1, List.of(new BitingTether()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Biting Tether")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IlluminatedFolio());
        harness.setHand(player1, List.of(new BitingTether()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target a shrouded creature with Biting Tether")
    void cannotTargetShroudedCreature() {
        Permanent creature = addCreatureReady(player2, new IslebackSpawn());

        harness.setHand(player1, List.of(new BitingTether()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Removing the Aura returns control but does not stop its pending upkeep counter")
    void pendingCounterResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new DuskUrchins());
        harness.setHand(player1, List.of(new BitingTether(), new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Biting Tether");
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Biting Tether");
        harness.assertOnBattlefield(player2, "Dusk Urchins");
        harness.assertNotOnBattlefield(player1, "Dusk Urchins");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
