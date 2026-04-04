package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RustedSlasherTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has sacrifice-an-artifact activated ability that grants regeneration")
    void hasCorrectAbilityStructure() {
        RustedSlasher card = new RustedSlasher();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeArtifactCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(RegenerateEffect.class);
    }

    // ===== Activation: sacrifice an artifact to regenerate =====

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));

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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Rusted Slasher from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Rusted Slasher (4/1) with regen shield blocks Grizzly Bears (2/2)
        Permanent slasher = addReadySlasher(player1);
        slasher.setRegenerationShield(1);
        slasher.setBlocking(true);
        slasher.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Rusted Slasher should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rusted Slasher"));
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

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rusted Slasher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rusted Slasher"));
    }

    // ===== Can sacrifice itself =====

    @Test
    @DisplayName("Rusted Slasher can sacrifice itself since it is an artifact")
    void canSacrificeItself() {
        addReadySlasher(player1);

        // Slasher is the only artifact — it is auto-sacrificed as cost
        // The ability goes on the stack but fizzles since the source left the battlefield
        harness.activateAbility(player1, 0, null, null);

        // Slasher was sacrificed as cost
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rusted Slasher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rusted Slasher"));

        // Ability is on the stack but will fizzle on resolution
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Stack resolves, ability fizzles
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadySlasher(Player player) {
        RustedSlasher card = new RustedSlasher();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
