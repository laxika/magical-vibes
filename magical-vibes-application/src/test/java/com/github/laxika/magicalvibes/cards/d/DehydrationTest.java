package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dehydration.class, DrakeHatchling.class, CreditVoucher.class})
class DehydrationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dehydration puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Dehydration.class);
    }

    @Test
    @DisplayName("Resolving Dehydration attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot cast Dehydration without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tapped creature with Dehydration does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());
        creature.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped creature with Dehydration remains untapped (Dehydration does not tap)")
    void untappedCreatureRemainsUntapped() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other permanents controlled by the same player still untap normally")
    void otherPermanentsStillUntap() {
        Permanent enchantedCreature = addCreatureReady(player2, new DrakeHatchling());
        enchantedCreature.tap();

        Permanent freeCreature = addCreatureReady(player2, new DrakeHatchling());
        freeCreature.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(enchantedCreature.getId());

        advanceToUpkeep(player2);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(freeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature stays tapped across multiple turns")
    void creatureStaysTappedAcrossMultipleTurns() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());
        creature.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);
        assertThat(creature.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();

        // Advance through player2's turn again — still tapped
        advanceToUpkeep(player2);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple Dehydrations on different creatures prevent both from untapping")
    void multipleDehydrationsOnDifferentCreatures() {
        Permanent creature1 = addCreatureReady(player2, new DrakeHatchling());
        creature1.tap();

        Permanent creature2 = addCreatureReady(player2, new DrakeHatchling());
        creature2.tap();

        Permanent dehydration1 = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydration1.setAttachedTo(creature1.getId());

        Permanent dehydration2 = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydration2.setAttachedTo(creature2.getId());

        advanceToUpkeep(player2);

        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature can untap again after Dehydration is removed")
    void creatureUntapsAfterDehydrationRemoved() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());
        creature.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(creature.getId());

        // Remove Dehydration
        gd.playerBattlefields.get(player1.getId()).remove(dehydrationPerm);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dehydration can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent creature = addCreatureReady(player1, new DrakeHatchling());

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Dehydration on own creature prevents it from untapping")
    void dehydrationOnOwnCreaturePreventsUntap() {
        Permanent creature = addCreatureReady(player1, new DrakeHatchling());
        creature.tap();

        Permanent dehydrationPerm = harness.addToBattlefieldAndReturn(player1, new Dehydration());
        dehydrationPerm.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dehydration fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before Dehydration resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Dehydration should be in graveyard, not on battlefield
        harness.assertInGraveyard(player1, "Dehydration");
        harness.assertNotOnBattlefield(player1, "Dehydration");
    }

    @Test
    @DisplayName("Can target a creature with Dehydration")
    void canTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrakeHatchling());
        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dehydration")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CreditVoucher());
        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Full integration: cast Dehydration on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        Permanent creature = addCreatureReady(player2, new DrakeHatchling());
        creature.tap();

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(creature.getId()));

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }
}

