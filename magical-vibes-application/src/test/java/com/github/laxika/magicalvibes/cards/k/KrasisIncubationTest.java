package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrasisIncubation.class, GrizzlyBears.class, BottleGnomes.class, FountainOfYouth.class,
        LlanowarElves.class})
class KrasisIncubationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new KrasisIncubation());
        aura.setAttachedTo(bears.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KrasisIncubation());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new KrasisIncubation());
        aura.setAttachedTo(gnomes.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new KrasisIncubation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Activated ability returns the Aura to hand and puts two +1/+1 counters on the enchanted creature")
    void abilityBouncesAuraAndAddsCounters() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KrasisIncubation());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Krasis Incubation");
        harness.assertInHand(player1, "Krasis Incubation");
    }

    @Test
    @DisplayName("Creature can attack once the Aura has been returned to hand")
    void creatureCanAttackAfterBounce() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new KrasisIncubation());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Casting the Aura attaches it to the chosen creature")
    void castingAuraAttachesToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KrasisIncubation()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Krasis Incubation").getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Returning the Aura is an immediate cost and restores the creature's abilities before resolution")
    void bounceCostRestoresAbilitiesBeforeResolution() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KrasisIncubation());
        aura.setAttachedTo(gnomes.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Krasis Incubation");
        harness.assertInHand(player1, "Krasis Incubation");
        assertThat(gnomes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        assertThat(gnomes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Aura can also put counters on its controller's creature")
    void abilityAddsCountersToControllersCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KrasisIncubation());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Krasis Incubation");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature cannot activate mana abilities")
    void enchantedCreatureCannotActivateManaAbilities() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new KrasisIncubation());
        aura.setAttachedTo(elves.getId());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(elves.isTapped()).isFalse();
    }
}
