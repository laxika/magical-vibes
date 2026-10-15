package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindTheMonster.class, HillGiant.class, GrizzlyBears.class, FountainOfYouth.class,
        Mistwalker.class, BrokenWings.class})
class BindTheMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Bind the Monster taps the creature and deals damage equal to its power to the Aura's controller")
    void enteringAuraTapsCreatureAndDealsPowerDamageToCaster() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BindTheMonster());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bind the Monster cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void alreadyTappedCreatureStillDealsDamage() {
        Permanent creature = addCreatureReady(player2, new Mistwalker());
        creature.tap();
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void removingAuraBeforeTriggerResolvesStillTapsCreatureAndDealsDamage() {
        Permanent creature = addCreatureReady(player2, new Mistwalker());
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Bind the Monster");
        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bind the Monster");
        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void removingCreatureBeforeTriggerResolvesStillDealsDamage() {
        Permanent creature = addCreatureReady(player2, new Mistwalker());
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Mistwalker");
        harness.assertNotOnBattlefield(player1, "Bind the Monster");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageUsesPowerWhenTriggerResolves() {
        Permanent creature = addCreatureReady(player2, new Mistwalker());
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new Mistwalker());
        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
    }
}
