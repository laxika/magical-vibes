package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlintHawkTest extends BaseCardTest {

    // ===== No artifacts — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices when controller has no artifacts")
    void autoSacrificesWithNoArtifacts() {
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        // Glint Hawk is NOT on the battlefield
        harness.assertNotOnBattlefield(player1, "Glint Hawk");

        // Glint Hawk is in the graveyard
        harness.assertInGraveyard(player1, "Glint Hawk");

        // No prompt — it was automatic
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== With artifact — accept bounce =====

    @Test
    @DisplayName("ETB with artifact on battlefield prompts may ability choice")
    void etbWithArtifactPromptsMayAbility() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability prompts for permanent choice")
    void acceptingMayAbilityPromptsPermanentChoice() {
        castGlintHawkWithArtifact();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Choosing artifact bounces it and keeps Glint Hawk")
    void choosingArtifactBouncesItAndKeepsGlintHawk() {
        castGlintHawkWithArtifact();

        harness.handleMayAbilityChosen(player1, true);

        UUID artifactId = findPermanent(player1, "Spellbook").getId();

        harness.handlePermanentChosen(player1, artifactId);

        // Glint Hawk is still on the battlefield
        harness.assertOnBattlefield(player1, "Glint Hawk");

        // Spellbook is no longer on the battlefield
        harness.assertNotOnBattlefield(player1, "Spellbook");

        // Spellbook is back in hand
        harness.assertInHand(player1, "Spellbook");
    }

    // ===== With artifact — decline bounce =====

    @Test
    @DisplayName("Declining may ability sacrifices Glint Hawk and keeps artifact")
    void decliningMayAbilitySacrificesGlintHawk() {
        castGlintHawkWithArtifact();

        harness.handleMayAbilityChosen(player1, false);

        // Glint Hawk is NOT on the battlefield
        harness.assertNotOnBattlefield(player1, "Glint Hawk");

        // Glint Hawk is in the graveyard
        harness.assertInGraveyard(player1, "Glint Hawk");

        // Spellbook is still on the battlefield
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    // ===== Multiple artifacts — only chosen one is bounced =====

    @Test
    @DisplayName("With multiple artifacts, only the chosen one is returned")
    void onlyChosenArtifactIsBounced() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB
        harness.handleMayAbilityChosen(player1, true);

        UUID scimitarId = findPermanent(player1, "Leonin Scimitar").getId();

        harness.handlePermanentChosen(player1, scimitarId);

        // Glint Hawk is on the battlefield
        harness.assertOnBattlefield(player1, "Glint Hawk");

        // Spellbook is still on the battlefield
        harness.assertOnBattlefield(player1, "Spellbook");

        // Leonin Scimitar was returned to hand
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInHand(player1, "Leonin Scimitar");
    }

    // ===== Opponent's artifacts don't count =====

    @Test
    @DisplayName("Opponent's artifacts don't satisfy the requirement")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        // Auto-sacrificed — no prompt
        harness.assertNotOnBattlefield(player1, "Glint Hawk");
        harness.assertInGraveyard(player1, "Glint Hawk");

        // Opponent's Spellbook is untouched
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    // ===== Helpers =====

    private void castGlintHawkWithArtifact() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        // Sanity check
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }
}
