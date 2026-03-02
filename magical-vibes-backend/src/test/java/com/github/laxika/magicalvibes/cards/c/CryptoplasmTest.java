package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoplasmTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Cryptoplasm has upkeep triggered copy ability")
    void hasCorrectAbilityStructure() {
        Cryptoplasm card = new Cryptoplasm();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(BecomeCopyOfTargetCreatureEffect.class);
    }

    // ===== Upkeep trigger: mandatory target selection =====

    @Test
    @DisplayName("Upkeep trigger presents target selection (not may prompt)")
    void upkeepTriggerPresentsTargetSelection() {
        addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        // Target selection is presented immediately — no may prompt first
        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Choosing target puts copy ability on the stack")
    void choosingTargetPutsAbilityOnStack() {
        addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetPermanentId())
                .isEqualTo(bearsId);
    }

    // ===== Resolution: may choice =====

    @Test
    @DisplayName("Accepting may on resolution makes Cryptoplasm a copy of the target")
    void acceptingMayMakesCopy() {
        Permanent cryptoplasm = addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept copy

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(cryptoplasm.getCard().getPower()).isEqualTo(2);
        assertThat(cryptoplasm.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining may on resolution does not change Cryptoplasm")
    void decliningMayDoesNotChangeCryptoplasm() {
        Permanent cryptoplasm = addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Cryptoplasm");
    }

    @Test
    @DisplayName("Copy retains upkeep copy ability (except it has this ability)")
    void copyRetainsUpkeepCopyAbility() {
        Permanent cryptoplasm = addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED))
                .anyMatch(e -> e instanceof BecomeCopyOfTargetCreatureEffect);
    }

    @Test
    @DisplayName("Copy acquires target creature's activated abilities")
    void copyAcquiresTargetAbilities() {
        Permanent cryptoplasm = addReadyCryptoplasm(player1);
        addReadyCreature(player2, new ProdigalPyromancer());
        UUID pyromancerId = harness.getPermanentId(player2, "Prodigal Pyromancer");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, pyromancerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getActivatedAbilities()).isNotEmpty();
    }

    @Test
    @DisplayName("Can copy again on subsequent upkeep after becoming a copy")
    void canCopyAgainOnSubsequentUpkeep() {
        Permanent cryptoplasm = addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player1, new ProdigalPyromancer());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // First upkeep: copy Grizzly Bears
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Grizzly Bears");

        // Second upkeep: copy Prodigal Pyromancer
        UUID pyromancerId = harness.getPermanentId(player1, "Prodigal Pyromancer");
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, pyromancerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Prodigal Pyromancer");
    }

    // ===== Targeting constraints =====

    @Test
    @DisplayName("Trigger does not fire when no other creatures exist")
    void triggerDoesNotFireWithNoOtherCreatures() {
        addReadyCryptoplasm(player1);
        // No other creatures on the battlefield

        advanceToUpkeep(player1);

        // The ability should not trigger at all (CR 603.3c: no legal target)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Can target opponent's creatures")
    void canTargetOpponentCreatures() {
        addReadyCryptoplasm(player1);
        addReadyCreature(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Copy effect fizzles if target is removed before resolution")
    void copyFizzlesIfTargetRemoved() {
        Permanent cryptoplasm = addReadyCryptoplasm(player1);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bearsId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passBothPriorities(); // resolve — target gone, no may prompt queued

        // Cryptoplasm should remain unchanged
        assertThat(cryptoplasm.getCard().getName()).isEqualTo("Cryptoplasm");
    }

    // ===== Helper methods =====

    private Permanent addReadyCryptoplasm(Player player) {
        Cryptoplasm card = new Cryptoplasm();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UNTAP → UPKEEP, triggers fire
    }
}
