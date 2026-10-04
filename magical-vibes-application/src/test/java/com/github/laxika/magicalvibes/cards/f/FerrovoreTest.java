package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ferrovore.class, Spellbook.class, LeoninScimitar.class, Memnite.class})
class FerrovoreTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spellbook");
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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
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

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

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
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
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

    @Test
    @DisplayName("The power boost expires after the turn ends")
    void boostExpiresAtEndOfTurn() {
        Permanent ferrovore = harness.addToBattlefieldAndReturn(player1, new Ferrovore());
        harness.addToBattlefield(player1, new Memnite());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(ferrovore.getEffectivePower()).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(ferrovore.getEffectivePower()).isEqualTo(2);
        assertThat(ferrovore.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped artifact creature can be sacrificed and the boost waits for resolution")
    void sacrificesTappedArtifactCreatureAsCost() {
        Permanent ferrovore = harness.addToBattlefieldAndReturn(player1, new Ferrovore());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        memnite.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Memnite");
        assertThat(ferrovore.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(ferrovore.getEffectivePower()).isEqualTo(5);
        assertThat(ferrovore.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player2, new Memnite());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Memnite");
        assertThat(gd.stack).isEmpty();
    }
}
