package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfDuskTest {

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
    @DisplayName("Knight of Dusk has correct card properties")
    void hasCorrectProperties() {
        KnightOfDusk card = new KnightOfDusk();

        assertThat(card.getName()).isEqualTo("Knight of Dusk");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Knight of Dusk has activated ability with correct properties")
    void hasActivatedAbility() {
        KnightOfDusk card = new KnightOfDusk();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{B}{B}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DestroyCreatureBlockingThisEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack and resolves to battlefield")
    void castingAndResolving() {
        harness.setHand(player1, List.of(new KnightOfDusk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Knight of Dusk");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Knight of Dusk"));
    }

    // ===== Activated ability: destroy creature blocking this =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting blocking creature")
    void activatingPutsOnStack() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Knight of Dusk");
        assertThat(entry.getTargetPermanentId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("Resolving ability destroys the blocking creature")
    void resolvingDestroysBlocker() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Ability consumes {B}{B} mana")
    void manaIsConsumed() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent blocker = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.activateAbility(player1, 0, null, blocker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent nonBlocker = addReadyCreature(player2);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonBlocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature blocking this creature");
    }

    @Test
    @DisplayName("Cannot target creature blocking a different attacker")
    void cannotTargetCreatureBlockingDifferentAttacker() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        // Add a second attacker at index 1
        Permanent otherAttacker = addReadyCreature(player1);
        otherAttacker.setAttacking(true);
        // Blocker is blocking the OTHER attacker (index 1), not the Knight (index 0)
        Permanent blocker = addBlocker(player2, 1);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature blocking this creature");
    }

    // ===== Can activate multiple times (no tap required) =====

    @Test
    @DisplayName("Can activate ability multiple times since it does not require tapping")
    void canActivateMultipleTimes() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent blocker1 = addBlocker(player2, 0);
        Permanent blocker2 = addBlockerAtIndex(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 4);

        // Activate twice before resolving — both go on the stack
        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.activateAbility(player1, 0, null, blocker2.getId());

        // Knight should NOT be tapped (ability doesn't require tap)
        assertThat(knight.isTapped()).isFalse();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);

        // Resolve both abilities
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving ability adds destruction to game log")
    void resolvingAddsToGameLog() {
        Permanent knight = addReadyKnight(player1);
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("destroyed"));
    }

    // ===== Helpers =====

    private Permanent addReadyKnight(Player player) {
        KnightOfDusk card = new KnightOfDusk();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBlocker(Player player, int attackerIndex) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBlockerAtIndex(Player player, int attackerIndex) {
        return addBlocker(player, attackerIndex);
    }

    private void setupCombatStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
