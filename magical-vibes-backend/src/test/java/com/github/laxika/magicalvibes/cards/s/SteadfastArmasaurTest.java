package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourceToughnessToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteadfastArmasaurTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has activated ability: {1}{W}, {T} deal toughness damage to creature in combat with it")
    void hasCorrectAbility() {
        SteadfastArmasaur card = new SteadfastArmasaur();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(DealDamageEqualToSourceToughnessToTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Target filter requires creature in combat with source")
    void hasCorrectTargetFilter() {
        SteadfastArmasaur card = new SteadfastArmasaur();

        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter())
                .isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter =
                (PermanentPredicateTargetFilter) card.getActivatedAbilities().getFirst().getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentAllOfPredicate.class);
        PermanentAllOfPredicate allOf = (PermanentAllOfPredicate) filter.predicate();
        assertThat(allOf.predicates()).hasSize(2);
        assertThat(allOf.predicates().get(0)).isInstanceOf(PermanentIsCreaturePredicate.class);
        assertThat(allOf.predicates().get(1)).isInstanceOf(PermanentInCombatWithSourcePredicate.class);
    }

    // ===== Deals damage when attacking and blocked =====

    @Test
    @DisplayName("Deals damage equal to toughness to a creature blocking it")
    void dealsToughnessDamageToBlocker() {
        Permanent armasaur = addReadyArmasaur(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setToughness(4);
        harness.addToBattlefield(player2, bears);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Armasaur has 3 toughness, so deals 3 damage to a 2/4 — survives
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Kills blocker when toughness damage is lethal")
    void killsBlockerWithLethalToughnessDamage() {
        Permanent armasaur = addReadyArmasaur(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Armasaur has 3 toughness, so deals 3 damage to a 2/2 — lethal
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Deals damage when blocking =====

    @Test
    @DisplayName("Deals damage equal to toughness to a creature it is blocking")
    void dealsToughnessDamageToAttackerItBlocks() {
        Permanent armasaur = addReadyArmasaur(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent attacker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurBlockingAttacker(armasaur, attacker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // Armasaur has 3 toughness, 3 damage kills a 2/2
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Cannot target creature not in combat with it =====

    @Test
    @DisplayName("Cannot target a creature not in combat with it")
    void cannotTargetCreatureNotInCombat() {
        Permanent armasaur = addReadyArmasaur(player1);
        armasaur.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Bears is not blocking the Armasaur, so it should not be a valid target
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Uses toughness, not power =====

    @Test
    @DisplayName("Damage is based on toughness — boosted toughness deals more damage")
    void damageUsesBoostedToughness() {
        Permanent armasaur = addReadyArmasaur(player1);
        armasaur.setPlusOnePlusOneCounters(2); // becomes 4/5

        GrizzlyBears bears = new GrizzlyBears();
        bears.setToughness(5);
        harness.addToBattlefield(player2, bears);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Toughness is 3 + 2 = 5, so deals 5 damage to a 2/5 — lethal
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Tap cost =====

    @Test
    @DisplayName("Taps when activating ability")
    void tapsOnActivation() {
        Permanent armasaur = addReadyArmasaur(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(armasaur.isTapped()).isTrue();
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent armasaur = addReadyArmasaur(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1); // needs {1}{W} = 2 total

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Activation puts ability on stack =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent armasaur = addReadyArmasaur(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Steadfast Armasaur");
    }

    // ===== Source removed before resolution =====

    @Test
    @DisplayName("Deals no damage if Armasaur is removed before resolution")
    void dealsNoDamageIfSourceRemoved() {
        Permanent armasaur = addReadyArmasaur(player1);
        GrizzlyBears bears = new GrizzlyBears();
        bears.setToughness(4);
        harness.addToBattlefield(player2, bears);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        // Remove Armasaur before ability resolves
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Ability resolves but source is gone, so no damage
        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        Permanent armasaur = addReadyArmasaur(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        // Remove target before ability resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyArmasaur(Player player) {
        SteadfastArmasaur card = new SteadfastArmasaur();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /**
     * Sets up combat where Armasaur (player1) is attacking and the given creature (player2) blocks it.
     * Armasaur has vigilance so it remains untapped.
     */
    private void setupArmasaurAttackingBlockedBy(Permanent armasaur, Permanent blocker) {
        armasaur.setAttacking(true);
        // Vigilance: Armasaur does NOT tap when attacking

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        int armasaurIndex = gd.playerBattlefields.get(player1.getId()).indexOf(armasaur);
        blocker.addBlockingTarget(armasaurIndex);
        blocker.addBlockingTargetId(armasaur.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    /**
     * Sets up combat where an opponent creature (player2) is attacking and Armasaur (player1) blocks it.
     */
    private void setupArmasaurBlockingAttacker(Permanent armasaur, Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        armasaur.setBlocking(true);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        armasaur.addBlockingTarget(attackerIndex);
        armasaur.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
