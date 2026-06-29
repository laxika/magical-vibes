package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MakeshiftMunitionsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts enchantment on the stack and resolves to battlefield")
    void castingResolvesToBattlefield() {
        harness.setHand(player1, List.of(new MakeshiftMunitions()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Makeshift Munitions"));
    }

    @Test
    @DisplayName("Activating with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a permanent to sacrifice puts ability on stack")
    void choosingPermanentPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        // Llanowar Elves should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
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

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability deals 1 damage to target creature on resolution")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        // Llanowar Elves is 1/1, so 1 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
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
        Permanent otherEnchantment = harness.addToBattlefieldAndReturn(player1, new Pacifism());
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
        UUID spellbookId = findPermanent(player1, "Spellbook").getId();
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        // Second activation — only one artifact left, auto-sacrifices
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not sacrifice Makeshift Munitions itself (excluded as source)")
    void doesNotSacrificeItself() {
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        // Only Makeshift Munitions is on battlefield — it's an enchantment, not an artifact or creature,
        // so it can't be sacrificed anyway. But if we add no valid targets, activation fails.
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
