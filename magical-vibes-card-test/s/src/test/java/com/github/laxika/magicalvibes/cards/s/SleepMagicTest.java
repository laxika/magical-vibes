package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleepMagic.class, GrizzlyBears.class, HillGiant.class, Shock.class, FountainOfYouth.class,
        Boomerang.class})
class SleepMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Sleep Magic taps the enchanted creature")
    void enteringAuraTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castSleepMagic(creature);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        castSleepMagic(creature);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sleep Magic is sacrificed when the enchanted creature is dealt damage")
    void auraIsSacrificedWhenEnchantedCreatureIsDealtDamage() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        castSleepMagic(creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sleep Magic")).isEmpty();
        assertThat(findPermanents(player2, "Hill Giant")).hasSize(1);
    }

    @Test
    @DisplayName("Sleep Magic cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new SleepMagic()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Damage to another creature does not sacrifice Sleep Magic")
    void damageToAnotherCreatureDoesNotSacrificeAura() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new HillGiant());
        castSleepMagic(enchanted);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, other.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sleep Magic");
        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The creature untaps normally after damage sacrifices Sleep Magic")
    void creatureUntapsAfterAuraIsSacrificed() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        castSleepMagic(creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sleep Magic");
        assertThat(creature.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enter trigger taps the creature even if Sleep Magic leaves first")
    void enterTriggerResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SleepMagic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sleep Magic");
        assertThat(creature.isTapped()).isFalse();
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertNotOnBattlefield(player1, "Sleep Magic");
        harness.assertInHand(player1, "Sleep Magic");
        assertThat(creature.isTapped()).isFalse();

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    private void castSleepMagic(Permanent creature) {
        harness.setHand(player1, List.of(new SleepMagic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
    }
}
