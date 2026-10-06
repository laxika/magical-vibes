package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustedSlasher.class, Spellbook.class, LeoninScimitar.class, GrizzlyBears.class})
class RustedSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another artifact grants Rusted Slasher a regeneration shield")
    void sacrificeArtifactGrantsRegenerationShield() {
        Permanent slasher = addReadySlasher(player1);
        harness.addToBattlefield(player1, new Spellbook());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // Slasher is also an artifact, so 2 artifacts available — must choose
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        // Spellbook should be sacrificed
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");

        // Rusted Slasher should have a regeneration shield
        assertThat(slasher.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield clears at end of turn")
    void regenerationShieldClearsAtEndOfTurn() {
        Permanent slasher = addReadySlasher(player1);
        harness.addToBattlefield(player1, new Spellbook());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(slasher.getRegenerationShield()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(slasher.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating with multiple other artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        addReadySlasher(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        // Slasher + Spellbook + Scimitar = 3 artifacts — must choose
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        addReadySlasher(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Does not require tap or mana to activate")
    void noManaCostNoTapRequired() {
        Permanent slasher = addReadySlasher(player1);
        slasher.tap();
        harness.addToBattlefield(player1, new Spellbook());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // No mana added, slasher is tapped — should still work since no tap/mana cost
        // 2 artifacts (slasher + spellbook) — must choose
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate multiple times per turn with multiple artifacts")
    void canActivateMultipleTimes() {
        Permanent slasher = addReadySlasher(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // First activation: 3 artifacts (slasher + spellbook + scimitar), must choose
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(slasher.getRegenerationShield()).isEqualTo(1);

        UUID scimitarId = findPermanent(player1, "Leonin Scimitar").getId();

        // Second activation: 2 artifacts left (slasher + scimitar), must choose
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, scimitarId);
        harness.passBothPriorities();

        assertThat(slasher.getRegenerationShield()).isEqualTo(2);

        // Both other artifacts should be gone
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Regeneration shield saves Rusted Slasher from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Rusted Slasher (4/1) with regen shield blocks Grizzly Bears (2/2)
        Permanent slasher = addReadySlasher(player1);
        slasher.setRegenerationShield(1);
        slasher.setBlocking(true);
        slasher.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Rusted Slasher should survive via regeneration
        harness.assertOnBattlefield(player1, "Rusted Slasher");
        Permanent survivedSlasher = findPermanent(player1, "Rusted Slasher");
        assertThat(survivedSlasher.isTapped()).isTrue();
        assertThat(survivedSlasher.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Rusted Slasher dies without regeneration shield in combat")
    void diesWithoutRegenerationShieldInCombat() {
        // Rusted Slasher (4/1) without regen shield blocks Grizzly Bears (2/2)
        Permanent slasher = addReadySlasher(player1);
        slasher.setBlocking(true);
        slasher.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rusted Slasher");
        harness.assertInGraveyard(player1, "Rusted Slasher");
    }

    @Test
    @DisplayName("Rusted Slasher can sacrifice itself since it is an artifact")
    void canSacrificeItself() {
        addReadySlasher(player1);

        // Slasher is the only artifact — it is auto-sacrificed as cost
        // The ability resolves without effect because the source left the battlefield.
        harness.activateAbility(player1, 0, null, null);

        // Slasher was sacrificed as cost
        harness.assertNotOnBattlefield(player1, "Rusted Slasher");
        harness.assertInGraveyard(player1, "Rusted Slasher");

        // The ability exists independently of its sacrificed source.
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // The ability resolves without returning the sacrificed creature.
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness does not prevent activation and the shield waits for resolution")
    void summoningSickSlasherPaysCostBeforeShieldResolves() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new RustedSlasher());
        slasher.setSummoningSick(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(slasher.getRegenerationShield()).isZero();
        assertThat(slasher.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(slasher.getRegenerationShield()).isEqualTo(1);
        assertThat(slasher.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only artifacts controlled by the activating player can pay the cost")
    void sacrificeChoicesExcludeNonArtifactsAndOpponentsArtifacts() {
        Permanent slasher = addReadySlasher(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(slasher.getId(), artifact.getId());
    }

    @Test
    @DisplayName("An activated shield replaces lethal damage, clears damage, and removes the blocker from combat")
    void activatedShieldRegeneratesInCombat() {
        Permanent slasher = addReadySlasher(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        slasher.setBlocking(true);
        slasher.addBlockingTarget(0);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rusted Slasher");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(slasher.getRegenerationShield()).isZero();
        assertThat(slasher.isTapped()).isTrue();
        assertThat(slasher.getMarkedDamage()).isZero();
        assertThat(slasher.isBlocking()).isFalse();
        assertThat(slasher.getBlockingTargets()).isEmpty();
    }

    private Permanent addReadySlasher(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RustedSlasher());
        perm.setSummoningSick(false);
        return perm;
    }

}
