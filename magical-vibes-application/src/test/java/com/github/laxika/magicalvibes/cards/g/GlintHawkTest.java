package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlintHawk.class, Memnite.class, AccordersShield.class, LiquimetalCoating.class})
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
        harness.addToBattlefield(player1, new Memnite());
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

        UUID artifactId = findPermanent(player1, "Memnite").getId();

        harness.handlePermanentChosen(player1, artifactId);

        // Glint Hawk is still on the battlefield
        harness.assertOnBattlefield(player1, "Glint Hawk");

        // Memnite is no longer on the battlefield
        harness.assertNotOnBattlefield(player1, "Memnite");

        // Memnite is back in hand
        harness.assertInHand(player1, "Memnite");
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

        // Memnite is still on the battlefield
        harness.assertOnBattlefield(player1, "Memnite");
    }

    // ===== Multiple artifacts — only chosen one is bounced =====

    @Test
    @DisplayName("With multiple artifacts, only the chosen one is returned")
    void onlyChosenArtifactIsBounced() {
        harness.addToBattlefield(player1, new Memnite());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, shield.getId());

        // Glint Hawk is on the battlefield
        harness.assertOnBattlefield(player1, "Glint Hawk");

        // Memnite is still on the battlefield
        harness.assertOnBattlefield(player1, "Memnite");

        // Accorder's Shield was returned to hand
        harness.assertNotOnBattlefield(player1, "Accorder's Shield");
        harness.assertInHand(player1, "Accorder's Shield");
    }

    // ===== Opponent's artifacts don't count =====

    @Test
    @DisplayName("Opponent's artifacts don't satisfy the requirement")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player2, new Memnite());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        // Auto-sacrificed — no prompt
        harness.assertNotOnBattlefield(player1, "Glint Hawk");
        harness.assertInGraveyard(player1, "Glint Hawk");

        // Opponent's Memnite is untouched
        harness.assertOnBattlefield(player2, "Memnite");
    }

    @Test
    @DisplayName("Glint Hawk can return itself after Liquimetal Coating makes it an artifact")
    void canReturnItselfWhenItBecomesAnArtifact() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID hawkId = harness.getPermanentId(player1, "Glint Hawk");

        harness.activateAbility(player1, 0, null, hawkId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(hawkId);
        harness.handlePermanentChosen(player1, hawkId);

        harness.assertInHand(player1, "Glint Hawk");
        harness.assertNotOnBattlefield(player1, "Glint Hawk");
        harness.assertNotInGraveyard(player1, "Glint Hawk");
        harness.assertOnBattlefield(player1, "Liquimetal Coating");
    }

    @Test
    @DisplayName("An artifact Glint Hawk can satisfy its trigger after Liquimetal Coating leaves")
    void canReturnItselfAsTheOnlyArtifact() {
        Permanent coating = harness.addToBattlefieldAndReturn(player1, new LiquimetalCoating());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID hawkId = harness.getPermanentId(player1, "Glint Hawk");

        harness.activateAbility(player1, 0, null, hawkId);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, coating));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, hawkId);

        harness.assertInHand(player1, "Glint Hawk");
        harness.assertNotOnBattlefield(player1, "Glint Hawk");
        harness.assertNotInGraveyard(player1, "Glint Hawk");
    }

    @Test
    @DisplayName("Sacrifices if its only artifact leaves before the trigger resolves")
    void sacrificesWhenArtifactLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, artifact));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glint Hawk");
        harness.assertNotOnBattlefield(player1, "Glint Hawk");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can return an artifact even after Glint Hawk leaves the battlefield")
    void canReturnArtifactAfterGlintHawkLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent hawk = findPermanent(player1, "Glint Hawk");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, hawk));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertInHand(player1, "Memnite");
        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Glint Hawk");
    }

    @Test
    @DisplayName("A controlled artifact is returned to its owner's hand")
    void returnsControlledArtifactToItsOwnersHand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        gd.stolenCreatures.put(artifact.getId(), player2.getId());
        harness.setHand(player1, List.of(new GlintHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertInHand(player2, "Memnite");
        harness.assertNotInHand(player1, "Memnite");
        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertOnBattlefield(player1, "Glint Hawk");
    }

    private void castGlintHawkWithArtifact() {
        harness.addToBattlefield(player1, new Memnite());
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
