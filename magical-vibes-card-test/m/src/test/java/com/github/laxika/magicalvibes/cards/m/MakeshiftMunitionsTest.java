package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.v.VraskaRelicSeeker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeshiftMunitions.class, Spellbook.class, LlanowarElves.class,
        LeoninScimitar.class, Pacifism.class})
class MakeshiftMunitionsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts enchantment on the stack and resolves to battlefield")
    void castingResolvesToBattlefield() {
        harness.setHand(player1, List.of(new MakeshiftMunitions()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Makeshift Munitions");
    }

    @Test
    @DisplayName("Activating with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating with one creature auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyCreature() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activating with multiple valid permanents asks to choose which to sacrifice")
    void asksForChoiceWithMultipleValidPermanents() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a permanent to sacrifice puts ability on stack")
    void choosingPermanentPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.assertNotOnBattlefield(player1, "Spellbook");
        // Llanowar Elves should still be on the battlefield
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Ability deals 1 damage to target player on resolution")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Ability deals 1 damage to target creature on resolution")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        // Llanowar Elves is 1/1, so 1 damage kills it
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact or creature to sacrifice")
    void cannotActivateWithoutValidSacrifice() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice an artifact or creature");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-creature permanent (enchantment)")
    void cannotSacrificeEnchantment() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent otherEnchantment = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        otherEnchantment.setAttachedTo(opponentCreature.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        // Only one "valid" permanent exists but it's an enchantment, so should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice an artifact or creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Can activate multiple times per turn with enough resources")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        // First activation — two valid permanents, so we get asked to choose
        harness.activateAbility(player1, 0, null, player2.getId());
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);

        // Second activation — only one artifact left, auto-sacrifices
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Unanimated Makeshift Munitions cannot pay the sacrifice cost")
    void doesNotSacrificeItself() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        // An enchantment without artifact or creature type cannot pay this cost.
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("Animated Makeshift Munitions can sacrifice itself and still deal damage")
    void animatedMunitionsCanSacrificeItself() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Makeshift Munitions");
        harness.assertInGraveyard(player1, "Makeshift Munitions");
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrificing the targeted creature leaves no legal target")
    void canSacrificeTargetedCreature() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability may target its controller")
    void canDamageItsController() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
    @Test
    @CardUsed({VraskaRelicSeeker.class})
    @DisplayName("Ability deals damage to a planeswalker by removing loyalty")
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent vraska = harness.addToBattlefieldAndReturn(player2, new VraskaRelicSeeker());
        harness.addMana(player1, ManaColor.RED, 1);
        int loyaltyBefore = vraska.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, null, vraska.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
        harness.assertOnBattlefield(player2, "Vraska, Relic Seeker");
    }
}
