package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FerrovoreTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Ability grants +3/+0 until end of turn on resolution")
    void boostsSelfOnResolution() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ferrovore = findPermanent(player1, "Ferrovore");
        // Base 2/2, boosted to 5/2
        assertThat(ferrovore.getEffectivePower()).isEqualTo(5);
        assertThat(ferrovore.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated multiple times per turn")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // First activation: 2 artifacts, must choose
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        // Second activation: 1 artifact left, auto-sacrificed
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ferrovore = findPermanent(player1, "Ferrovore");
        // Base 2/2, boosted twice: 2 + 3 + 3 = 8
        assertThat(ferrovore.getEffectivePower()).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not require tap to activate (can use when summoning sick)")
    void doesNotRequireTap() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        // Ferrovore is summoning sick but ability doesn't require tap
        Permanent ferrovore = findPermanent(player1, "Ferrovore");
        assertThat(ferrovore.isSummoningSick()).isTrue();

        // Should succeed despite summoning sickness
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Base 2/2, boosted to 5/2
        assertThat(ferrovore.getEffectivePower()).isEqualTo(5);
    }

}
