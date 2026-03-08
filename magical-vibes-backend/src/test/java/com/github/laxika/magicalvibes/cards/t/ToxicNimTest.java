package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToxicNimTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Toxic Nim has regenerate activated ability")
    void hasRegenerateAbility() {
        ToxicNim card = new ToxicNim();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{B}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Toxic Nim puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ToxicNim()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Toxic Nim");
    }

    @Test
    @DisplayName("Resolving Toxic Nim puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new ToxicNim()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Toxic Nim");
    }

    // ===== Regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent perm = addToxicNimReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Toxic Nim");
        assertThat(entry.getTargetPermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addToxicNimReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent nim = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(nim.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Infect: combat damage deals poison to player =====

    @Test
    @DisplayName("Unblocked Toxic Nim deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent perm = addToxicNimReady(player1);
        perm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged (infect deals poison, not life loss)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (4)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    // ===== Infect: combat damage deals -1/-1 counters to creatures =====

    @Test
    @DisplayName("Toxic Nim deals -1/-1 counters to blocking creature")
    void dealsMinusCountersToBlocker() {
        // Grizzly Bears is 2/2
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Toxic Nim is 4/1
        Permanent atkPerm = addToxicNimReady(player1);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(com.github.laxika.magicalvibes.model.AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Toxic Nim (4/1) dies to Grizzly Bears (2/2) — 2 damage >= 1 toughness
        harness.assertNotOnBattlefield(player1, "Toxic Nim");
        harness.assertInGraveyard(player1, "Toxic Nim");

        // Grizzly Bears gets 4 -1/-1 counters (from 4 infect damage), making it -2/-2 → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== Regeneration saves from lethal combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Toxic Nim from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Toxic Nim (4/1) with regen shield blocks Grizzly Bears (2/2)
        Permanent nimPerm = addToxicNimReady(player1);
        nimPerm.setRegenerationShield(1);
        nimPerm.setBlocking(true);
        nimPerm.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Toxic Nim should survive via regeneration
        harness.assertOnBattlefield(player1, "Toxic Nim");
        Permanent nim = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Toxic Nim"))
                .findFirst().orElseThrow();
        // Regeneration should tap the creature
        assertThat(nim.isTapped()).isTrue();
        // Regeneration shield should be consumed
        assertThat(nim.getRegenerationShield()).isEqualTo(0);

        // Grizzly Bears gets 4 -1/-1 counters from infect damage → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Toxic Nim dies in combat without regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent nimPerm = addToxicNimReady(player1);
        nimPerm.setBlocking(true);
        nimPerm.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Toxic Nim should be dead (1 toughness, 2 damage from Grizzly Bears)
        harness.assertNotOnBattlefield(player1, "Toxic Nim");
        harness.assertInGraveyard(player1, "Toxic Nim");
    }

    // ===== Helper methods =====

    private Permanent addToxicNimReady(Player player) {
        ToxicNim card = new ToxicNim();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
