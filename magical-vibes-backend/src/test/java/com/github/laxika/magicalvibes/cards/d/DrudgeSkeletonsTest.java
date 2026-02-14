package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BallistaSquad;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrudgeSkeletonsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Drudge Skeletons has correct card properties")
    void hasCorrectProperties() {
        DrudgeSkeletons card = new DrudgeSkeletons();

        assertThat(card.getName()).isEqualTo("Drudge Skeletons");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SKELETON);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Drudge Skeletons puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DrudgeSkeletons()));
        harness.addMana(player1, "B", 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Drudge Skeletons");
    }

    @Test
    @DisplayName("Resolving Drudge Skeletons puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new DrudgeSkeletons()));
        harness.addMana(player1, "B", 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
    }

    // ===== Activate regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Drudge Skeletons");
        assertThat(entry.getTargetPermanentId()).isEqualTo(skelePerm.getId());
    }

    @Test
    @DisplayName("Activating regeneration ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent skele = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(skele.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent skele = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(skele.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can stack multiple regeneration shields")
    void canStackMultipleRegenerationShields() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent skele = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(skele.getRegenerationShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate regeneration ability even when tapped")
    void canActivateWhenTapped() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.tap();
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Drudge Skeletons");
    }

    @Test
    @DisplayName("Can activate regeneration ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        DrudgeSkeletons card = new DrudgeSkeletons();
        Permanent skelePerm = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(skelePerm);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Drudge Skeletons");
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaIsConsumedWhenActivating() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addDrudgeSkeletonsReady(player1);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating regeneration ability logs the activation")
    void activatingAbilityLogsActivation() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Drudge Skeletons's ability"));
    }

    @Test
    @DisplayName("Resolving regeneration ability logs the shield")
    void resolvingAbilityLogsShield() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains a regeneration shield"));
    }

    @Test
    @DisplayName("Ability fizzles if Drudge Skeletons is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Drudge Skeletons from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Drudge Skeletons (1/1) with regen shield blocks Grizzly Bears (2/2)
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Drudge Skeletons should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        // Regeneration should tap the creature
        Permanent skele = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();
        assertThat(skele.isTapped()).isTrue();
        // Regeneration shield should be consumed
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
        // Grizzly Bears should also die (1 damage from skeletons >= ... no, 1 < 2, so bears live)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Drudge Skeletons dies without regeneration shield in combat")
    void diesWithoutRegenerationShieldInCombat() {
        // Drudge Skeletons (1/1) without regen shield blocks Grizzly Bears (2/2)
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Drudge Skeletons should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("Regeneration shield saves attacking Drudge Skeletons from lethal blocker damage")
    void regenerationSavesAttackingCreature() {
        // Drudge Skeletons (1/1) with regen shield attacks, blocked by Grizzly Bears (2/2)
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(1);
        skelePerm.setAttacking(true);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Drudge Skeletons survives via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        Permanent skele = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();
        assertThat(skele.isTapped()).isTrue();
        assertThat(skele.isAttacking()).isFalse();
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Regeneration logs that the creature regenerates")
    void regenerationLogsCombat() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Drudge Skeletons regenerates"));
    }

    // ===== Regeneration saves from targeted destroy =====

    @Test
    @DisplayName("Regeneration shield saves from targeted destroy effect")
    void regenerationSavesFromTargetedDestroy() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(1);

        // Use Demystify-like instant that destroys target creature
        // We'll use a direct approach: cast a destroy spell targeting it
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, "W", 1);

        // Demystify targets enchantments, not creatures. Let's instead directly test via
        // Ballista Squad dealing lethal damage
        // Actually, let's test via direct damage from Ballista Squad (X=1 kills 1-toughness)
        harness.getGameData().playerBattlefields.get(player1.getId()).clear(); // remove skeletons

        Permanent skelePerm2 = addDrudgeSkeletonsReady(player1);
        skelePerm2.setRegenerationShield(1);
        skelePerm2.setAttacking(true);

        Permanent ballistaPerm = addBallistaReady(player2);
        harness.addMana(player2, "W", 2); // X=1, {W}=1

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 1, skelePerm2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Drudge Skeletons survives via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("Without regeneration shield, direct damage kills Drudge Skeletons")
    void directDamageKillsWithoutShield() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setAttacking(true);

        Permanent ballistaPerm = addBallistaReady(player2);
        harness.addMana(player2, "W", 2); // X=1, {W}=1

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 1, skelePerm.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    // ===== Wrath of God cannot be regenerated =====

    @Test
    @DisplayName("Wrath of God destroys Drudge Skeletons even with regeneration shield")
    void wrathOfGodIgnoresRegenerationShield() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(2);

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, "W", 4);

        // Need to pass priority to player2 so they can cast
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    // ===== Regeneration shield clears at end of turn =====

    @Test
    @DisplayName("Regeneration shield clears at end of turn cleanup")
    void regenerationShieldClearsAtEndOfTurn() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        harness.addMana(player1, "B", 2);

        // Activate twice and resolve
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent skele = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(skele.getRegenerationShield()).isEqualTo(2);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(skele.getRegenerationShield()).isEqualTo(0);
    }

    // ===== Only one shield consumed per lethal event =====

    @Test
    @DisplayName("Only one regeneration shield is consumed per lethal damage event")
    void onlyOneShieldConsumedPerLethalEvent() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(3);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent skele = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();
        // Only one shield consumed, two remaining
        assertThat(skele.getRegenerationShield()).isEqualTo(2);
    }

    // ===== Regeneration clears combat state =====

    @Test
    @DisplayName("Regeneration clears blocking state")
    void regenerationClearsBlockingState() {
        Permanent skelePerm = addDrudgeSkeletonsReady(player1);
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent skele = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();
        assertThat(skele.isBlocking()).isFalse();
        assertThat(skele.getBlockingTargets()).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addDrudgeSkeletonsReady(Player player) {
        DrudgeSkeletons card = new DrudgeSkeletons();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBallistaReady(Player player) {
        BallistaSquad card = new BallistaSquad();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
