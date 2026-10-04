package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypervoltGrasp.class, DryadSophisticate.class, IzzetSignet.class})
class HypervoltGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to any target")
    void enchantedCreatureDealsDamageToAnyTarget() {
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can deal damage to an opposing creature")
    void enchantedCreatureDealsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());
        addCreatureReady(player2, new DryadSophisticate());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Dryad Sophisticate"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dryad Sophisticate");
        harness.assertNotOnBattlefield(player2, "Dryad Sophisticate");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the Aura's ability returns it to its owner's hand")
    void returnsAuraToHand() {
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hypervolt Grasp");
        harness.assertNotOnBattlefield(player1, "Hypervolt Grasp");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantANoncreaturePermanent() {
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new IzzetSignet());
        harness.setHand(player1, List.of(new HypervoltGrasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting the Aura grants the ability to the chosen creature")
    void castingAuraGrantsAbility() {
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());
        harness.setHand(player1, List.of(new HypervoltGrasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hypervolt Grasp");
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The opposing creature's controller activates the granted ability")
    void opposingCreatureControllerActivatesGrantedAbility() {
        Permanent creature = addCreatureReady(player2, new DryadSophisticate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(creature.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hypervolt Grasp");
        harness.assertOnBattlefield(player2, "Dryad Sophisticate");
    }

    @Test
    @DisplayName("The granted tap ability cannot be used with summoning sickness")
    void summoningSicknessPreventsGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DryadSophisticate());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An ordinary artifact is not a legal damage target")
    void grantedAbilityCannotTargetOrdinaryArtifact() {
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returning the Aura does not counter an already activated damage ability")
    void damageAbilityResolvesAfterAuraReturnsToHand() {
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HypervoltGrasp());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Hypervolt Grasp");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Dryad Sophisticate");
    }
}
